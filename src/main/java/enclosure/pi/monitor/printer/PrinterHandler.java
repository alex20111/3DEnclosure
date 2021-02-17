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
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fazecast.jSerialComm.SerialPort;
import com.jsoniter.output.JsonStream;

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
	private SerialPort comPort;
	private InputStream in;

	private Path filePath;
	private StringBuilder outputs = new StringBuilder();

	boolean keepingConnectionAlive = true;

	private boolean isConnected = false;
	private boolean serialConnStarted = false;

	private boolean forceStopped = false;
	private PrintMode mode = PrintMode.NOT_PRINTING;	

	private Thread printingThread;
	private Thread printerListeningThread;

	private boolean sdCardReady = false;
	private List<String> fileList = new ArrayList<>();
	private LocalDateTime prevSdCardReading = null;

	private PrintServiceData printData = new PrintServiceData();

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

		filePath= Paths.get("/opt/jetty/PrinterData"+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")) + ".txt");

		connect();
	}	

	private void connect() {
		new Thread(new Runnable() {

			@Override
			public void run() {

				while(keepingConnectionAlive) {

					try {
						logger.debug("Serial is open: " + (comPort != null ? comPort.isOpen() : "false") );

						if (comPort == null || !comPort.isOpen()) {

							comPort = SerialPort.getCommPort("/dev/serial/by-path/platform-3f980000.usb-usb-0:1.2:1.0-port0");

							comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
							comPort.setBaudRate(115200);
							comPort.setNumDataBits(8);
							comPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
							comPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);

							boolean portOpen = comPort.openPort();

							if (portOpen) {
								logger.info("Connected to printer");
								isConnected = true;
								in = comPort.getInputStream();

								Thread.sleep(6000);
								startListening();
								sendCommand("M155 S5", false);//send a command to get the hot end and bed temp every 10 sec 
							}
							else {
								isConnected = false;
								serialConnStarted = false;
								logger.info("Could not connect to printer , retrying in 15 sec. " );
							}

						}else if (comPort != null && comPort.isOpen() && !mode.isPrinting()) {
							verifyPrinterConnected();
						}else if (comPort != null && comPort.isOpen() && mode == PrintMode.SD_PRINTING ) {

							if (prevSdCardReading == null) {
								prevSdCardReading = LocalDateTime.now().plusMinutes(5);
							}

							if (LocalDateTime.now().isAfter(prevSdCardReading)) {
								prevSdCardReading.plusSeconds(30);
								//send periodic file status when SD is printing.
								sendCommand("M27", false); //get file remaining byte to calculare % every 30 sec
							}
						}
					}catch(Exception ex) {
						logger.error("Thread printer: " , ex);
					}
					try {
						Thread.sleep(15000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}

		}).start();
	}

	public void sendGcodeFileToPrinter(FileList file) throws FileNotFoundException, IllegalAccessError {

		if (isConnected) {		

			if (printingThread == null && !mode.isPrinting()) {

				Path gcode = new File(Constants.GCODE_DIR+ file.getFileName()).toPath();

				if (Files.exists(gcode)) {

					forceStopped = false;

					printingThread = new Thread(new Runnable() {
						@Override
						public void run() {

//							printData = new PrintServiceData();
							printData.setPrintFile(file);
							printData.setPrinting(true);
							mode = PrintMode.PI_PRINTING;
							
							File gcodeFile = gcode.toFile();
							
							 long totalLength = gcodeFile.length();
						        double lengthPerPercent = 100.0 / totalLength;
						        long readLength = 0;

							outputs.append("!!! -- Starting print -- !!!\n");
							try (
									BufferedReader objReader = new BufferedReader(new FileReader(gcodeFile))){

								String str;
								while ((str = objReader.readLine()) != null && mode.isPrinting()) {
									
									//calculate percent complete
									int len = str.length() + 2;
									readLength += len;
									printData.setPercentComplete((int)Math.round(readLength * lengthPerPercent));									
									
									if (!str.startsWith(";") && len > 0) {

										sendCommand(str, true);

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
								}

								logger.debug("Looop done printing sending websocket message");
								printData.setPrintFinished();
								SocketMessage msg = new SocketMessage(WsAction.SEND, DataType.PRINT_DONE, JsonStream.serialize(printData) );
								WebSocketClient.getInstance().sendMessage(msg);
								//send SMS message
								ThreadManager.getInstance().sendSmsMessage(new SendSMSThread("Printing Done!", "Your printing is finished."));

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

							outputs.append("!!! == print thread finished == !!!\n");
							writeOutputs();
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
		if (isConnected) {

			if (!mode.isPrinting()) {

				try {
					//					sendCommand("M111 S1", true);
					//select file
					sendCommand("M23 " + file.getFileName(), true); //select file name

					//sent print command
					sendCommand("M24", true);  
					printData.setPrinting(true);
					printData.setPrintStarted(LocalDateTime.now().toString());
					printData.setPercentComplete(0);
					printData.setPrintFile(file);

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
	 */
	public List<String> getSdCardFileList() {
		logger.debug("Getting SD Card file List");

		fileList.clear();
		sdCardReady = false;
		try {
			for(int i = 0; i < 6; i++) {
				if (!sdCardReady) {
					logger.debug("checking if sd card ready");
					sendCommand("M21", true);  //see if the SD card is ready
				}
				else if (sdCardReady) {
					break;
				}
			}
			logger.debug("sd carc ready:  " + sdCardReady);
			if (sdCardReady) {

				sendCommand("M20", true);	// list files on the card.			
			}else {
				logger.error("cannot initialize SD card  ");
			}

		}catch(Exception ex) {
			logger.error("Message acessing SD card" , ex);
		}
		writeOutputs();
		return fileList;
	}

	public void stopPrinting() {
		logger.debug("Stopping printing!!");
		outputs.append("\n!!! == Stop print == !!!\n");
		try {
			if (mode == PrintMode.PI_PRINTING) {

				logger.debug("Stopping PI Printing!!");

				mode = PrintMode.NOT_PRINTING;
				if (printingThread != null) {
					printingThread.interrupt();
					forceStopped = true;

					try {
						printingThread.join(3000);
					} catch (InterruptedException e) {
						logger.debug("Error ing", e);
					}

					logger.debug("Starting stop printing commands");
					printData.printAborded();
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
						sendCommand(str, true);
					}

					mode = PrintMode.NOT_PRINTING; 
				}
			}else if (mode == PrintMode.SD_PRINTING) {
				logger.debug("Stopping SD Printing!!");
				printData.printAborded();
				//do stop code here
				mode = PrintMode.NOT_PRINTING; //TODO  stop initiated.. bed and nozzle still heating.. will stop after..

				sendCommand("M524", false);  // this is the SD stop
			}
		}catch(InterruptedException | IOException ex) {
			logger.error("Error in stopping printer", ex);
		}
		outputs.append("!!! == Stop print END  == !!!\n");
		writeOutputs();
	}

	public PrintServiceData getPrintData() {
		return this.printData;
	}
	public boolean isConnected() {
		return isConnected;
	}
	public boolean isPrinting() {
		return mode.isPrinting();
	}

	public void emergencyStop() {
		logger.info("Sending emergency stop ");
		try {
			sendCommand("M112", false);
		} catch (IOException | InterruptedException e) {
			logger.error("error emergencyStop" , e);
		}
	}


	private void sendCommand(String command, boolean wait) throws IOException, InterruptedException {
		outputs.append("Writing: " + command + "\n");
//		logger.debug("Writing: " + command );
		String s2cmd = command + "\r\n";
		byte[] toB = s2cmd.getBytes();
		comPort.writeBytes(toB, toB.length);

		if (wait) {
			synchronized (monitor) {
				monitor.wait(20000);
			}
		}
	}
	private boolean verifyPrinterConnected() { 	
		String s2cmd = "M111\r\n";
		byte[] toB = s2cmd.getBytes();
		comPort.writeBytes(toB, toB.length);
		boolean answer = false;

		LocalDateTime breakNowFuture = LocalDateTime.now().plusSeconds(5);	

		try {
			synchronized (monitor) {
				monitor.wait(5500);
			}

			if(LocalDateTime.now().isAfter(breakNowFuture)) {
				logger.debug("No printer connteted");
				isConnected = false;
				answer = false;
				//			break;
			}
		}catch(InterruptedException ie) {
			logger.info("verifyPrinterConnected was interrupted", ie );
			isConnected = false;
			answer = false;
		}
		return answer;
	}

	private void sendTempData(String evnt) {
//		logger.debug("sendTempData: " + evnt);
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

			SocketMessage msg = new SocketMessage(WsAction.SEND,DataType.PRINT_DATA, JsonStream.serialize(printData));
			WebSocketClient.getInstance().sendMessage(msg);
		}catch (Exception e) {
			logger.error("Exception in sendTempData" , e);
		}
	}

	private void sendPercentCompleted(String line) {
//		logger.debug("sendPercentCompleted: " + line);
		try {
			String subStr = line.substring(line.indexOf("byte") + 4, line.length()).trim();
			String bytesRemaining[] = subStr.split("/");		

			double completed = Double.parseDouble(bytesRemaining[0]);
			double total = Double.parseDouble(bytesRemaining[1]);
			double percentDbl = (completed / total) * 100;
			int percent = (int)percentDbl;

			printData.setPercentComplete(percent);
			SocketMessage msg = new SocketMessage(WsAction.SEND,DataType.PRINT_DATA, JsonStream.serialize(printData));
			WebSocketClient.getInstance().sendMessage(msg);
		}catch(Exception e) {
			logger.info("Problem with percentage conversion" , e);
		}
	}

	private void writeOutputs() {
		try {
			Files.write(filePath, outputs.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			outputs = new StringBuilder();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Thread that listen to com events.
	 */
	private void startListening() {

		if (printerListeningThread == null) {
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
										outputs.append("Response: " + line + "\n");
//										logger.debug("Response: " + line );
										if (line.startsWith("ok") &&  !line.contains("T:") && !line.contains("B:") ) {
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
										}
									}
								}
							}
						}catch(IOException e) {
							logger.error("Error in listening thread" , e);
							break;
						}
					}
					logger.debug("printer listening thread ended");
					printerListeningThread = null;
				}

			});

			printerListeningThread.start();
			try {
				Thread.sleep(100);

				//wait until got good data
				for(int i=0 ; i < 5 ; i ++) {
					sendCommand("M111", true);
					if (serialConnStarted) {
						break;
					}
					Thread.sleep(500);
				}
			} catch (InterruptedException e) {	} catch (IOException e) {}
		}else {
			logger.debug("printer listening thread already started");
		}
		logger.debug("listenet started");
	}
	private void finalizeSdPrinting() {
		logger.debug("Finishing SD printing");
		mode = PrintMode.NOT_PRINTING;
		printData.setPrintFinished();

		SocketMessage msg = new SocketMessage(WsAction.SEND,DataType.PRINT_DONE, JsonStream.serialize(printData) );
		WebSocketClient.getInstance().sendMessage(msg);

		//send SMS message
		ThreadManager.getInstance().sendSmsMessage(new SendSMSThread("Printing Done!", "Your printing is finished."));
		writeOutputs();
	}
}
