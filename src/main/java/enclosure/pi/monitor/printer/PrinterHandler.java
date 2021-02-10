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

	private SerialPort comPort;
	private InputStream in;

	private Path filePath;
	private StringBuilder outputs = new StringBuilder();

	boolean keepingConnectionAlive = true;

	private boolean isConnected = false;
	private boolean forceStopped = false;
	private PrintMode mode = PrintMode.NOT_PRINTING;	

	private Thread printingThread;

	private boolean sdCardReady = false;
	private List<String> fileList = new ArrayList<>();

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
							}
							else {
								logger.info("Could not connect to printer , retrying in 10 sec. " );
							}

						}else if (comPort != null && comPort.isOpen() && !mode.isPrinting()) {
							verifyPrinterConnected();
						}
					}catch(Exception ex) {
						logger.error("Thread printer: " , ex);
					}
					try {
						Thread.sleep(10000);
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

							printData = new PrintServiceData();
							printData.setPrintFile(file);
							printData.setPrinting(true);
							mode = PrintMode.PI_PRINTING;

							outputs.append("!!! -- Starting print -- !!!\n");
							try (
									BufferedReader objReader = new BufferedReader(new FileReader(gcode.toFile()))){

								String str;
								while ((str = objReader.readLine()) != null && mode.isPrinting()) {
									if (!str.startsWith(";") && str.length() > 0) {

										sendCommand(str, true);

									}else if (str.contains("TIME") && !str.contains("TIME_ELAPSED")) {
										logger.debug("Send time info: ");
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
								printData.setPrintTimeSeconds(0);
								printData.setPrinting(false);
								printData.setPrinterBusy(false);
								SocketMessage msg = new SocketMessage(WsAction.SEND, DataType.PRINT_DONE, "Print Done!!" );
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
							printData = new PrintServiceData();

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

	public void startPrintFromSD(FileList file){
		if (isConnected) {

			if (!mode.isPrinting()) {
				mode = PrintMode.SD_PRINTING;
				try {
					//select file
					sendCommand("M23 " + file.getFileName(), true);

					//sent print command
					sendCommand("M24", true);  //TODO

				} catch (IOException | InterruptedException e) {
					logger.error("Error in SD print" , e);
				}  //start print

			}else {
				logger.debug("Thread is not null or is printing: " + (mode == PrintMode.PI_PRINTING ? " Pi Printing " : " SD printing") );
			}
			//			createSerialListener();
		}else {
			throw new IllegalAccessError("Printer is not connected");
		}
	}

	public List<String> getSdCardFileList() {
		logger.debug("Getting SD Card file List");

		fileList.clear();
		sdCardReady = false;
		try {
			for(int i = 0; i < 10; i++) {
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
	public boolean isConnected() {
		return isConnected;
	}
	public boolean isPrinting() {
		return mode.isPrinting();
	}

	public void stopPrinting() {
		logger.debug("Stopping printing!!");
		outputs.append("\n!!! == Stop print == !!!\n");

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
				printData = new PrintServiceData();
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

				try {
					for (String str : endGcode) {
						sendCommand(str, true);
					}
					mode = PrintMode.NOT_PRINTING; 
				}catch(Exception ex) {
					logger.error("Error in stopping printer", ex);
				}
			}
		}else if (mode == PrintMode.SD_PRINTING) {
			logger.debug("Stopping SD Printing!!");
			//do stop code here
			mode = PrintMode.NOT_PRINTING; //TODO  stop initiated.. bed and nozzle still heating.. will stop after..
		}
		outputs.append("!!! == Stop print END  == !!!\n");
		writeOutputs();
	}

	public PrintServiceData getPrintData() {
		return this.printData;
	}

	private void sendCommand(String command, boolean wait) throws IOException, InterruptedException {
		outputs.append("Writing: " + command + "\n");
		String s2cmd = command + "\r\n";
		byte[] toB = s2cmd.getBytes();
		comPort.writeBytes(toB, toB.length);


		if (wait) {
			boolean okFound = false;

			while(!okFound) {
				if (comPort.bytesAvailable() > -1) {
					try(BufferedReader br=new BufferedReader(new InputStreamReader(in,	StandardCharsets.UTF_8))){
						boolean readingFileList = false;
						String line;
						while( (line = br.readLine() ) != null && !forceStopped) {
							outputs.append("Response: " + line + "\n");
							logger.debug("response: " + line);

							if (line.startsWith("ok")) {
								okFound = true;
								break;
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
							}
						}

						if (forceStopped) {
							throw new InterruptedException("Forced stopped command");
						}
					}
				}
			}
		}
	}
	private boolean verifyPrinterConnected() { 	
		String s2cmd = "M31\r\n";
		byte[] toB = s2cmd.getBytes();
		comPort.writeBytes(toB, toB.length);
		boolean okFound = false;
		boolean answer = false;

		LocalDateTime breakNowFuture = LocalDateTime.now().plusSeconds(5);	

		while(!okFound) {
			if (comPort.bytesAvailable() > -1) {
				try(BufferedReader br=new BufferedReader(new InputStreamReader(in,	StandardCharsets.UTF_8))){

					String line;
					while( (line = br.readLine() ) != null) {

						if (line.contains("ok")) {
							okFound = true;
							answer = true;
							break;						
						}
					}
				}catch(IOException e) {
					logger.error("Error in verifying if printer exist: " , e);
				}
			}

			if(LocalDateTime.now().isAfter(breakNowFuture)) {
				logger.debug("No printer connteted");
				isConnected = false;
				answer = false;
				break;
			}
		}
		return answer;
	}

	private void sendTempData(String evnt) {
		logger.debug("sendTempData: " + evnt);
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

	private void writeOutputs() {
		try {
			Files.write(filePath, outputs.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			outputs = new StringBuilder();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//	private  void createSerialListener() {
	//
	//		listener = new SerialPortDataListener() {
	//
	//			//		comPort.addDataListener(new SerialPortDataListener() {
	//
	//			@Override
	//			public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }
	//
	//			@Override
	//			public void serialEvent(SerialPortEvent event)
	//			{
	//				if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
	//					return;
	//
	//				boolean okFound = false;
	//				while(!okFound ) {			
	//					if (comPort.bytesAvailable() > -1) {
	//
	//						
	//						try(BufferedReader br=new BufferedReader(new InputStreamReader(in,	StandardCharsets.UTF_8))){
	//							boolean readingFileList = false;
	//							String line;
	//							while( (line = br.readLine() ) != null ) {
	////								System.out.println("Line out: " + line);
	//								outputs.append("\nRecieved: " + line);
	//								if (line.startsWith("ok")) {
	//									synchronized (monitor) {
	//										monitor.notifyAll();
	//
	//									}
	//									break;
	//								}else if(line.contains("SD card ok")) {
	//									sdCardReady = true;
	//								}else if(line.contains("Begin file list")) {
	//									readingFileList = true;
	//								}else if (line.contains("End file list")) {
	//									readingFileList = false;							
	//								}else if (readingFileList) {
	//									fileList.add(line.split(" ")[0]);
	//								}
	//								if (interrupt) {
	//									System.out.println("break");
	//									okFound = true;
	//									break;
	//								}
	//							
	//							}
	//						}catch(IOException ex) {
	//							ex.printStackTrace();
	//						}
	//					}
	//	
	//				} 
	//
	//			}
	//		};
	//
	//		comPort.addDataListener(listener);
	//	}
}
