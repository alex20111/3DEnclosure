package enclosure.pi.monitor.printer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortIOException;
import com.jsoniter.output.JsonStream;

import enclosure.pi.monitor.arduino.ExtractorFan;
import enclosure.pi.monitor.arduino.ExtractorFan.ExtractorFanCmd;
import enclosure.pi.monitor.common.Constants;
import enclosure.pi.monitor.service.model.FileList;
import enclosure.pi.monitor.service.model.PrintServiceData;
import enclosure.pi.monitor.thread.SendSMSThread;
import enclosure.pi.monitor.thread.ThreadManager;
import enclosure.pi.monitor.websocket.DataType;
import enclosure.pi.monitor.websocket.SocketMessage;
import enclosure.pi.monitor.websocket.WebSocketClient;
import enclosure.pi.monitor.websocket.WsAction;

public class PrinterHandler {

	private static final Logger logger = LogManager.getLogger(PrinterHandler.class);

	private static PrinterHandler printerHandler;

	private Object monitor = new Object();
	private Object pauseThread = new Object();

	private SerialPort comPort;
	private InputStream in;

	private Path filePath;
	private BlockingQueue<String> serialQueue;	
	private boolean serialConnStarted = false;

	private boolean forceStopped = false;
	private PrintMode mode = PrintMode.NOT_PRINTING;	

	private Thread printingThread;
	private Thread printerListeningThread;

	private boolean sdCardReady = false;
	private List<String> fileList = new ArrayList<>();
	private LocalDateTime prevSdCardReading = LocalDateTime.now();

	private LocalDateTime lastSerialHeartBeat = null; //last time that we got a serial return..

	private PrintServiceData printData = new PrintServiceData();

	//files bytes 
	private long fileBytesProcessed = -1;

	public static PrinterHandler getInstance() {
		if (printerHandler == null) {
			synchronized (PrinterHandler.class) {
				if(printerHandler == null) {
					logger.info( "printerHandler initialized");
					printerHandler = new PrinterHandler();
				}
			}
		}
		return printerHandler;
	}	

	private PrinterHandler() { 
		logger.debug("Starting: PrinterHandler");

		connect();
	}	

	private void connect() {
		new Thread(new Runnable() {

			@Override
			public void run() {

				while(true) {

					try {
						logger.debug("Serial is open: " + (comPort != null ? comPort.isOpen() : "false") );

						if (comPort == null || !comPort.isOpen()) {
							//platform-3f980000.usb-usb-0:1.2:1.0-port0 
							comPort = SerialPort.getCommPort("/dev/serial/by-path/platform-3f980000.usb-usb-0:1.2:1.0-port0");

							comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
							comPort.setBaudRate(115200);
							comPort.setNumDataBits(8);
							comPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
							comPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);

							boolean portOpen = comPort.openPort();

							if (portOpen) {
								logger.info("Connecting to printer");
								//1st check if connection is initialized

								printData.setPrinterConnected(true); 
								in = comPort.getInputStream();

								filePath= Paths.get("/opt/jetty/PrinterData"+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")) + ".txt");
								serialQueue = new ArrayBlockingQueue<String>(1000);
								serialQueue.add("Starting print instance");
								ThreadManager.getInstance().startPrinterSerialListener(serialQueue, filePath);

								startListening();
								sendCommand("M155 S5", 0);//send a command to get the hot end and bed temp every 10 sec 

							}
							else {
								printData.setPrinterConnected(false);								 
								serialConnStarted = false;

								ThreadManager.getInstance().stopPrinterSerialListener();
								logger.info("Could not connect to printer , retrying in 15 sec. " );
							}

						}else if (comPort != null && comPort.isOpen() && !mode.isPrinting()) {
							verifyPrinterConnected();
						}else if (comPort != null &&
								comPort.isOpen() && mode == PrintMode.SD_PRINTING && 
								printData.isPrintingModel() && 
								!printData.isPrintPaused()) {


							if (prevSdCardReading != null && LocalDateTime.now().isAfter(prevSdCardReading)) {
								prevSdCardReading.plusSeconds(30);
								//send periodic file status when SD is printing.
								sendCommand("M27", 0); //get file remaining byte to calculare % every 30 sec
							}
						}
					}catch(Exception ex) {
						logger.error("Thread printer: " , ex);
					}
					try {
						Thread.sleep(8000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}
		}).start();
	}

	public void sendGcodeFileToPrinter(FileList file) throws FileNotFoundException, IllegalAccessError {

		if (printData.isPrinterConnected()) {		

			if (printingThread == null && !mode.isPrinting()) {

				Path gcode = new File(Constants.GCODE_DIR+ file.getFileName()).toPath();

				if (Files.exists(gcode)) {

					forceStopped = false;

					printingThread = new Thread(new Runnable() {
						@Override
						public void run() {

							printData.startPiPrinting(file);
							mode = PrintMode.PI_PRINTING;

							File gcodeFile = gcode.toFile();

							long totalLength = gcodeFile.length();
							double lengthPerPercent = 100.0 / totalLength;
							fileBytesProcessed = 0;

							try (BufferedReader objReader = new BufferedReader(new FileReader(gcodeFile))){

								String str;
								while ((str = objReader.readLine()) != null && mode.isPrinting()) {

									//calculate percent complete
									int len = str.length() + 2;
									fileBytesProcessed += len;
									printData.setPercentComplete((int)Math.round(fileBytesProcessed * lengthPerPercent));									

									if (!str.startsWith(";") && str.trim().length() > 0) {

										sendCommand(str, 1200000);

									}else if (str.contains("TIME") && !str.contains("TIME_ELAPSED")) {
										try {
											String timeStr = str.substring(str.indexOf(":") + 1, str.length()).trim();
											double timeInSec = Double.parseDouble(timeStr);
											int sec = (int)Math.round(timeInSec);
											logger.debug("Send time info: timeStr:  " + timeStr + " timeInSec: " + timeInSec + " sec: " + sec );

											printData.setPrintTimeSeconds(sec + Constants.STARTING_OFFSET);
											printData.setPrintStarted(LocalDateTime.now().toString());
											SocketMessage msg = new SocketMessage(WsAction.SEND,DataType.PRINT_TOTAL_TIME, JsonStream.serialize(printData));
											WebSocketClient.getInstance().sendMessage(msg);

										}catch(NumberFormatException nfx) {}
									}

									if (forceStopped) {
										throw new InterruptedException("Forced interrupted by user");
									}
									if (printData.isPrintPaused()) { //pause the print..
										synchronized (pauseThread) {
											pauseThread.wait();
										}
									}									
								}

								finalizePiPrinting();

							}catch(IOException e) {
								stopPrinting(); 
								logger.error("Error in sendGcodeFileToPrinter" , e);
							}
							catch (InterruptedException e) {
								logger.debug("thread inturrepted", e);
								Thread.currentThread().interrupt();
							}
							mode = PrintMode.NOT_PRINTING;

							printingThread = null;

							logger.debug("Printer thread finished");
						}
					});
					printingThread.start();
				}else {
					throw new FileNotFoundException("File does not exist");
				}
			}else {
				//TODO thread is still running, stop it or notify to stop
				logger.debug("Thread is not null or is printing: " + (mode == PrintMode.PI_PRINTING ? " Pi Printing " : " SD printing"));
			}

		}else {
			throw new IllegalAccessError("Printer is not connected");
		}
	}

	public void startPrintFromSD(FileList file) throws IllegalAccessError{
		if (printData.isPrinterConnected()) {

			if (!mode.isPrinting()) {

				try {
					fileBytesProcessed = 0;
					//select file
					sendCommand("M23 " + file.getFileName(), 20000); //select file name

					//sent print command
					sendCommand("M24", 20000);  
					printData.startSDPrinting(file);

					SocketMessage msg = new SocketMessage(WsAction.SEND,DataType.PRINT_TOTAL_TIME, JsonStream.serialize(printData));
					WebSocketClient.getInstance().sendMessage(msg);

					mode = PrintMode.SD_PRINTING;

				} catch (IOException | InterruptedException e) {
					logger.error("Error in SD print" , e);
					throw new IllegalAccessError("Error in SD print");
				} 

			}else {
				logger.debug("Thread is not null or is printing: " + (mode == PrintMode.PI_PRINTING ? " Pi Printing " : " SD printing") );
				throw new IllegalAccessError("Printer is printing");
			}
		}else {
			throw new IllegalAccessError("Printer is not connected");
		}
	}
	/**
	 * Get a list of files on the sd card
	 * @return
	 * @throws IOException 
	 */
	public List<String> getSdCardFileList() throws IOException {
		logger.debug("Getting SD Card file List");

		if(printData.isPrinterConnected()) {

			fileList.clear();
			sdCardReady = false;
			try {
				for(int i = 0; i < 6; i++) {
					if (!sdCardReady) {
						logger.debug("checking if sd card ready");
						sendCommand("M21", 20000);  //see if the SD card is ready
					}
					else if (sdCardReady) {
						break;
					}
				}

				if (sdCardReady) {

					sendCommand("M20", 20000);	// list files on the card.			
				}else {
					logger.error("cannot initialize SD card  ");
				}

			}catch(Exception ex) {
				logger.error("Message acessing SD card" , ex);
			}
		}else {
			throw new IOException("Printer not connected, cannot get files");
		}
		return fileList;
	}

	public void stopPrinting() {
		logger.debug("Stopping printing!!");
		try {
			if (mode == PrintMode.PI_PRINTING) {

				logger.debug("Stopping PI Printing!!");

				mode = PrintMode.NOT_PRINTING;
				printData.printAborded();
				if (printingThread != null) {
					printingThread.interrupt();
					forceStopped = true;

					try {
						printingThread.join(3000);
					} catch (InterruptedException e) {
						logger.debug("Error ing", e);
					}

					logger.debug("Starting stop printing commands");
					printingThread = null;

					forceStopped = false; // reset so we can send commands..

					List<String> endGcode = new ArrayList<>();
					endGcode.add("M108");
					endGcode.add("M140 S0");
					endGcode.add("M107");
					endGcode.add("G91 ;Relative positionning");
					endGcode.add("G1 E-2 F2700 ;Retract a bit");
					endGcode.add("G1 E-2 Z0.2 F2400 ;Retract and raise Z");
					endGcode.add("G1 X5 Y5 F3000 ;Wipe out");
					endGcode.add("G1 Z10 ;Raise Z more");
					endGcode.add("G90 ;Absolute positionning");
					endGcode.add("M106 S0 ;Turn-off fan");
					endGcode.add("M104 S0 ;Turn-off hotend");
					endGcode.add("M140 S0 ;Turn-off bed");
					endGcode.add("M84 X Y E ;Disable all steppers but Z");
					endGcode.add("M82 ;absolute extrusion mode");
					endGcode.add("M104 S0");

					for (String str : endGcode) {
						sendCommand(str, 1200000);
					}

					mode = PrintMode.NOT_PRINTING; 
				}
			}else if (mode == PrintMode.SD_PRINTING) {
				logger.debug("Stopping SD Printing!!");
				printData.printAborded();
				//do stop code here
				mode = PrintMode.NOT_PRINTING; //TODO  stop initiated.. bed and nozzle still heating.. will stop after..

				sendCommand("M524", 0);  // this is the SD stop
			}
		}catch(InterruptedException | IOException ex) {
			logger.error("Error in stopping printer", ex);
		}
	}

	public PrintServiceData getPrintData() {
		return this.printData;
	}
	public boolean isPrinting() {
		return mode.isPrinting();
	}

	public void emergencyStop() {
		logger.info("Sending emergency stop ");
		try {
			printData.printAborded();
			sendCommand("M112", 0);
		} catch (IOException | InterruptedException e) {
			logger.error("error emergencyStop" , e);
		}
	}

	//get the number of bytes processed by the printer for the file.
	public long getFileBytesProcessed() {
		return fileBytesProcessed;
	}
	public void pausePrint() throws IOException, InterruptedException {
		if (isPrinting()) {
			if (mode == PrintMode.SD_PRINTING) {
				sendCommand("M25", 5000);
				printData.setPrintPaused(true);
			}else {
				printData.setPrintPaused(true);		
			}
		}
	}
	public void resumePrint() throws IOException, InterruptedException {
		if (isPrinting()) {
			if (mode == PrintMode.SD_PRINTING) {
				sendCommand("M108", 5000);
				printData.setPrintPaused(false);
			}else {
				printData.setPrintPaused(false);
				synchronized (pauseThread) {
					pauseThread.notifyAll();
				}
			}
		}
	}

	public void sendCommand(String command, int wait) throws IOException, InterruptedException {

		serialQueue.offer("Writing: " + command + "\n");
		String s2cmd = command + "\r\n";
		byte[] toB = s2cmd.getBytes();
		comPort.writeBytes(toB, toB.length);

		if (wait > 0) {
			synchronized (monitor) {
				monitor.wait(wait);
			}
		}
	}
	/**
	 * Return the log file name and path
	 * @return
	 */
	public Path getPrintLogFile()
	{
		return this.filePath;
	}
	private boolean verifyPrinterConnected() { 	

		LocalDateTime nowMinus20Sec = LocalDateTime.now().minusSeconds(20);

		logger.debug("verifyPrinterConnected. lastSerialHeartBeat: " + lastSerialHeartBeat + " - nowMinus20Sec: " + nowMinus20Sec);


		if (lastSerialHeartBeat != null && lastSerialHeartBeat.isAfter(nowMinus20Sec)) {
			return true;
		}

		logger.debug("verifyPrinterConnected no heart beat.. printer not active");

		return false;

	}

	private void sendTempData(String evnt) {
		try {
			String strSplit[] = evnt.trim().split(" ");

			String nozzle = strSplit[0].substring(strSplit[0].indexOf("T:") + 2 , strSplit[0].length()).trim();
			String nozzleMax = strSplit[1].substring(strSplit[1].indexOf("/") + 1 , strSplit[1].length()).trim();
			String bed = strSplit[2].substring(strSplit[2].indexOf("B:") + 2 , strSplit[2].length()).trim();
			String bedMax = strSplit[3].substring(strSplit[3].indexOf("/") + 1 , strSplit[3].length()).trim();
			printData.setBedTemp(Float.valueOf(bed));
			printData.setBedTempMax(Float.valueOf(bedMax));
			printData.setNozzleTemp(Float.valueOf(nozzle));
			printData.setNozzleTempMax(Float.valueOf(nozzleMax));

		}catch (Exception e) {
			logger.error("Exception in sendTempData" , e);
		}
	}

	private void sendPercentCompleted(String line) {
		try {
			String subStr = line.substring(line.indexOf("byte") + 4, line.length()).trim();
			String bytesRemaining[] = subStr.split("/");		

			double completed = Double.parseDouble(bytesRemaining[0]);
			fileBytesProcessed = (long)completed;
			double total = Double.parseDouble(bytesRemaining[1]);
			double percentDbl = (completed / total) * 100;
			int percent = (int)percentDbl;

			printData.setPercentComplete(percent);
		}catch(Exception e) {
			logger.info("Problem with percentage conversion" , e);
		}
	}

	/**
	 * Thread that listen to com events.
	 */
	private void startListening() {

		if (printerListeningThread == null || !printerListeningThread.isAlive()) {
			logger.debug("Starting thread");

			printerListeningThread = new Thread(new Runnable() {

				@Override
				public void run() {
					while(true) {
						try {
							if (comPort.bytesAvailable() > -1) {
								try(BufferedReader br=new BufferedReader(new InputStreamReader(in,	StandardCharsets.UTF_8))){
									boolean readingFileList = false;
									String line;
									while( (line = br.readLine() ) != null) {
										lastSerialHeartBeat = LocalDateTime.now();
										serialQueue.offer(line);
										//																				logger.debug("Response: " + line );
										if (line.startsWith("ok") ) {
											synchronized (monitor) {
												monitor.notifyAll();
											}
										}else if (line.contains("T:") && line.contains("B:")) {
											sendTempData(line);
										}else if(line.contains("SD card ok")) {
											sdCardReady = true;
										}else if(line.contains("Begin file list")) {
											readingFileList = true;
										}else if (line.contains("End file list")) {
											readingFileList = false;							
										}else if (readingFileList) {
											fileList.add(line.split(" ")[0]);
										}else if(line.contains("echo:DEBUG:")) {
											serialConnStarted = true;
										}else if (line.contains("SD printing byte")) { //M27
											sendPercentCompleted(line);
										}else if (line.contains("Done printing file") && mode == PrintMode.SD_PRINTING) {
											finalizeSdPrinting();
										}else if (line.contains("Printing Started") && printData.isPrinting()) {
											printData.setPrintingModel(true);
										}else if (line.contains("Printing finished")) {
											printData.setPrintingModel(false);
										}
									}
								}
							}
						}catch(SerialPortIOException se) {
							logger.error("Error in listening thread SerialPortIOException" , se);
							//							serialConnStarted = false;
							//							comPort.closePort();
							//							printData.setPrinterConnected(false);
							//							break;
						}catch(IOException e) {
							logger.error("Error in listening thread" , e);
							serialConnStarted = false;
							break;
						}
					}
					try {
						in.close();
					} catch (IOException e) {
						logger.error("error closing comm input stream", e);
					}
					logger.debug("printer listening thread ended");
					printerListeningThread = null;
				}
			});

			printerListeningThread.start();

		}else {
			logger.debug("printer listening thread already started");
		}

		try {
			Thread.sleep(100);

			//wait until got good data
			for(int i=0 ; i < 5 ; i ++) {
				sendCommand("M111", 20000);
				if (serialConnStarted) {
					break;
				}
				Thread.sleep(500);
			}
		} catch (InterruptedException e) {	} catch (IOException e) {}

		logger.debug("listenet started");
	}
	private void finalizeSdPrinting() {
		logger.debug("Finishing SD printing");
		mode = PrintMode.NOT_PRINTING;
		printData.setPrintFinished();

		SocketMessage msg = new SocketMessage(WsAction.SEND,DataType.PRINT_DONE, JsonStream.serialize(printData) );
		WebSocketClient.getInstance().sendMessage(msg);

		//send SMS message
		ThreadManager tm = ThreadManager.getInstance();
		tm.sendSmsMessage(new SendSMSThread("Printing Done!", "Your printing is finished."));

		if (printData.isAutoPrinterShutdown()) { 
			tm.shutdownPrinter(5, TimeUnit.MINUTES);
		}
		try {
			//set fan to 100%
			ExtractorFan fan = new ExtractorFan(ExtractorFanCmd.SET_SPEED);
			fan.setFanSpeed(100);
		}catch (IOException e) {
			logger.error("Error for Extractor Fan: " + e.getMessage());
		}
	}
	private void finalizePiPrinting() {

		logger.debug("Looop done printing sending websocket message");
		printData.setPrintFinished();

		if (printData.isAutoPrinterShutdown()) {  
			ThreadManager.getInstance().shutdownPrinter(5, TimeUnit.MINUTES);
		}
		SocketMessage msg = new SocketMessage(WsAction.SEND, DataType.PRINT_DONE, JsonStream.serialize(printData) );
		WebSocketClient.getInstance().sendMessage(msg);
		//send SMS message
		ThreadManager.getInstance().sendSmsMessage(new SendSMSThread("Printing Done!", "Your printing is finished."));

		try {
			//set fan to 100%
			ExtractorFan fan = new ExtractorFan(ExtractorFanCmd.SET_SPEED);
			fan.setFanSpeed(100);
		}catch (IOException e) {
			logger.error("Error for Extractor Fan: " + e.getMessage());
		}
	}
}

// Printing Started
//M118 Printing finished
