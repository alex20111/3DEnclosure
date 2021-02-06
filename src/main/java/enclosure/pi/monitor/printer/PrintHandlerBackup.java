package enclosure.pi.monitor.printer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
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

import com.jsoniter.output.JsonStream;
import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.StopBits;

import enclosure.pi.monitor.common.Constants;
import enclosure.pi.monitor.service.model.PrintServiceData;
import enclosure.pi.monitor.thread.SendSMSThread;
import enclosure.pi.monitor.thread.ThreadManager;
import enclosure.pi.monitor.websocket.DataType;
import enclosure.pi.monitor.websocket.SocketMessage;
import enclosure.pi.monitor.websocket.WebSocketClient;
import enclosure.pi.monitor.websocket.WsAction;

public class PrintHandlerBackup {

	private static final Logger logger = LogManager.getLogger(PrinterHandler.class);

	private static PrintHandlerBackup printerHandler;

	private Serial serial;
	private SerialConfig config;

	private Path filePath;

	boolean keepingConnectionAlive = true;
	private printerSerialListener listener = null;

	private boolean isConnected = false;
	private boolean sendingGcode = false;
	private boolean printing = false;

	private static Object monitor = new Object();
	//	private StringBuilder outputs = new StringBuilder();

	private Thread printingThread;

	private PrintServiceData printData = new PrintServiceData();

	public static PrintHandlerBackup getInstance() {
		if (printerHandler == null) {
			synchronized (PrintHandlerBackup.class) {
				if(printerHandler == null) {
					logger.info( "printerHandler initialized");
					printerHandler = new PrintHandlerBackup();
				}
			}
		}
		return printerHandler;
	}	

	private PrintHandlerBackup() { 
		logger.debug("Starting: PrinterHandler");

		filePath= Paths.get("/opt/jetty/PrinterData"+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")) + ".txt");

		connect();
	}	

	private void connect() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String usbPort = "/dev/serial/by-path/platform-3f980000.usb-usb-0:1.2:1.0-port0";

				serial = SerialFactory.createInstance();
				config = new SerialConfig();
				config.device(usbPort)
				.baud(Baud._115200)
				.dataBits(DataBits._8)
				.parity(Parity.NONE)
				.stopBits(StopBits._1)
				.flowControl(FlowControl.NONE);

				while(keepingConnectionAlive) {
					try {
						logger.debug("Serial is open: " + serial.isOpen() + " Closed: " + serial.isClosed() );

						if (!serial.isOpen()) {
							try {
								serial.open(config);

								if (listener != null && serial != null) {
									logger.debug("Printer listener not null, removing");
									serial.removeListener(listener);
									listener = null;
								}

								listener = new printerSerialListener();
								serial.addListener(listener);
								logger.info("Connected to printer");
								isConnected = true;

							} catch (IOException e) {
								logger.info("Could not connect to printer , retrying in 5 sec. Message: " + e.getMessage() );
							}
						}else if (serial != null && serial.isOpen()) {
							//test if can get RTS
							try {
								boolean ok = serial.getDSR();
//								logger.debug("dsr: " + ok);						

							}catch(IOException | IllegalStateException e) {
								logger.info("Cannot contact printer, disconnecting");
								isConnected = false;
								serial.close();
							}
						}
					}catch(Exception ex) {
						logger.error("Thread printer: " , ex);
					}
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}

		}).start();
	}

	public void sendGcodeFileToPrinter(String file) throws FileNotFoundException, IllegalAccessError {

		//		outputs = new StringBuilder("Starting printing");

		if (isConnected) {
			if (printingThread == null && !printing) {

				Path gcode = new File(Constants.GCODE_DIR+ file).toPath();

				if (Files.exists(gcode)) {
					printing = true;

					//					RandomAccessFile gcodeFile = new RandomAccessFile(gcode.toFile().getAbsoluteFile(), "r");

					monitor = new Object();
					printData = new PrintServiceData();
					printData.setPrintFile(file);
					printData.setPrinting(true);

					printingThread = new Thread(new Runnable() {

						@Override
						public void run() {

							BufferedReader objReader = null;

							try {
								objReader = new BufferedReader(new FileReader(gcode.toFile()));
								sendingGcode = true;
								String str;
								while ((str = objReader.readLine()) != null && sendingGcode) {
									if (!str.startsWith(";") && str.length() > 0) {
										//										outputs.append("\nWriting: " + str);
										serial.write(str + "\r\n");										
										synchronized(monitor) {							
											monitor.wait();							
										}

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
								//								printing =  false;
								//								sendingGcode = false;
								stopPrinting(); 
								//								writeOutputs();
								logger.error("Error in sendGcodeFileToPrinter" , e);
							}catch (InterruptedException e) {
								logger.debug("thread inturrepted", e);
								Thread.currentThread().interrupt();
								//								writeOutputs();

							}
							printing =  false;
							sendingGcode = false;

							//							writeOutputs();
							printingThread = null;
							printData = new PrintServiceData();

							try {
								if (objReader != null) {
									objReader.close();
								}
							}catch(IOException e) {
								logger.error("Problem closing file", e);
							}

							logger.debug("Printer thread finished");
						}
					});
					printingThread.start();
				}else {
					throw new FileNotFoundException("File does not exist");
				}
			}else {
				//TODO thread is still running, stop it or notify to stop
				logger.debug("Thread is not null or is printing: " + printing);
			}
		}else {
			throw new IllegalAccessError("Printer is not connected");
		}
	}


	public boolean isConnected() {
		return isConnected;
	}


	public boolean isPrinting() {
		return printing;
	}

	public void stopPrinting() {
		sendingGcode = false;

		if (printingThread != null) {
			printingThread.interrupt();

			try {
				printingThread.join(2000);
			} catch (InterruptedException e) {
				logger.debug("Error ing", e);
			}

			logger.debug("Starting stop printing commands");
			printData = new PrintServiceData();
			//			outputs = new StringBuilder();
			printingThread = null;

			monitor = new Object();

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
			//			endGcode.add("G1 X0 Y235 ;Present print");
			endGcode.add("M106 S0 ;Turn-off fan");
			endGcode.add("M104 S0 ;Turn-off hotend");
			endGcode.add("M140 S0 ;Turn-off bed");
			endGcode.add("M84 X Y E ;Disable all steppers but Z");
			endGcode.add("M82 ;absolute extrusion mode");
			endGcode.add("M104 S0");

			sendingGcode = true;
			try {
				for (String str : endGcode) {
					//					outputs.append("\nWrite stop: " + str);
					serial.write(str + "\r\n" );
					synchronized(monitor) {							
						monitor.wait();							
					}
				}

				printing = false;
				sendingGcode = false;
				//				writeOutputs();
			}catch(Exception ex) {
				logger.error("Error in stopping printer", ex);
			}
		}


	}

	public PrintServiceData getPrintData() {
		return this.printData;
	}

	//	private void writeOutputs() {
	//		try {
	//			Files.write(filePath, outputs.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
	//		} catch (IOException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//	}

	class printerSerialListener implements SerialDataEventListener{

		@Override
		public void dataReceived(SerialDataEvent event) {
			try {
				String eventString =  event.getAsciiString();

				if (sendingGcode) {
					if (eventString.contains("ok")){

						synchronized(monitor) {
							monitor.notifyAll();
						}
					}else {
						if (eventString.contains("T:") && eventString.contains("B:")) {
							sendTempData(eventString);
						}
					}
				}


			}catch(Exception ex) {
				logger.error("error in dataReceived", ex);
			}

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

	}
}
