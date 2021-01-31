package enclosure.pi.monitor.printer;

import java.io.File;
import java.io.FileNotFoundException;
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
import enclosure.pi.monitor.websocket.SocketMessage;
import enclosure.pi.monitor.websocket.WebSocketClient;
import enclosure.pi.monitor.websocket.WsAction;

public class PrinterHandler {

	private static final Logger logger = LogManager.getLogger(PrinterHandler.class);

	private static PrinterHandler printerHandler;

	private Serial serial;
	private SerialConfig config;

	private Path filePath;

	boolean cmdNotSent = true;
	int cnt = 0;

	boolean keepingConnectionAlive = true;
	private printerSerialListener listener = null;

	private boolean isConnected = false;
	private boolean sendingGcode = false;
	private boolean printing = false;

	private static Object monitor = new Object();
	private StringBuilder outputs = new StringBuilder();

	private Thread printingThread;
	//TODO push pre-heat sequence to angular as well as time.

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
								logger.debug("dsr: " + ok);						

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

		outputs = new StringBuilder("Starting printing");
		
		if (isConnected) {
			if (printingThread == null && !printing) {

				Path gcode = new File(Constants.GCODE_DIR+ file).toPath();

				if (Files.exists(gcode)) {
					printing = true;
					RandomAccessFile gcodeFile = new RandomAccessFile(gcode.toFile().getAbsoluteFile(), "r");

					printingThread = new Thread(new Runnable() {
						
						@Override
						public void run() {
							
							try {
								sendingGcode = true;
								String str;
								while ((str = gcodeFile.readLine()) != null && sendingGcode) {
									if (!str.startsWith(";") && str.length() > 0) {
										serial.write(str + "\r\n");
										synchronized(monitor) {							
											monitor.wait();							
										}

									}else if (str.contains("TIME")) {
										try {
											double timeInSec = Double.parseDouble(str.substring(str.indexOf(":") + 1, str.length()));
											int sec = (int)Math.round(timeInSec);
											int p1 = sec % 60;
											int p2 = sec / 60;
											int p3 = p2 % 60;
											p2 = p2 / 60;
											//				        output.append("Time remaining: " +  p2 + ":" + p3 + ":" + p1);
											System.out.print( p2 + ":" + p3 + ":" + p1);
											System.out.print("\n");
											SocketMessage msg = new SocketMessage(WsAction.SEND, "Time: "+ p2 + ":" + p3 + ":" + p1);
											WebSocketClient.getInstance().sendMessage(msg);
										}catch(NumberFormatException nfx) {

										}
									}
								}
								printing =  false;
								sendingGcode = false;
								gcodeFile.close();
								logger.debug("Looop done printing sending websocket message");
								SocketMessage msg = new SocketMessage(WsAction.SEND, "Printing Done!");
								WebSocketClient.getInstance().sendMessage(msg);
							}catch(IOException e) {
								printing =  false;
								sendingGcode = false;
//								stopPrinting(); 
								logger.error("Error in sendGcodeFileToPrinter" , e);
							}catch (InterruptedException e) {
								Thread.currentThread().interrupt();
								sendingGcode = false;
							
							}
							
							writeOutputs();
							printingThread = null;
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
//		monitor.notifyAll();
		if (printingThread != null) {
			printingThread.interrupt();
			
			try {
				printingThread.join(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			printingThread = null;
			
			List<String> endGcode = new ArrayList<>();
			endGcode.add("M140 S0\r\n");
			endGcode.add("M107\r\n");
			endGcode.add("G91 ;Relative positionning\r\n");
			endGcode.add("G1 E-2 F2700 ;Retract a bi");
			endGcode.add("G1 E-2 Z0.2 F2400 ;Retract and raise Z\r\n");
			endGcode.add("G1 X5 Y5 F3000 ;Wipe out\r\n");
			endGcode.add("G1 Z10 ;Raise Z more\r\n");
			endGcode.add("G90 ;Absolute positionning\r\n");
			endGcode.add("G1 X0 Y235 ;Present print\r\n");
			endGcode.add("M106 S0 ;Turn-off fan\r\n");
			endGcode.add("M104 S0 ;Turn-off hotend\r\n");
			endGcode.add("M140 S0 ;Turn-off bed\r\n");
			endGcode.add("M84 X Y E ;Disable all steppers but Z\r\n");
			endGcode.add("M82 ;absolute extrusion mode\r\n");
			endGcode.add("M104 S0\r\n");

			try {
				for (String str : endGcode) {
					serial.write(str );
					synchronized(monitor) {							
						monitor.wait();							
					}
				}

				printing = false;
				writeOutputs();
			}catch(Exception ex) {
				logger.error("Error in stopping printer", ex);
			}
		}

		
	}
	
	private void writeOutputs() {
		try {
			Files.write(filePath, outputs.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	class printerSerialListener implements SerialDataEventListener{

		@Override
		public void dataReceived(SerialDataEvent event) {
			try {
				String eventString =  event.getAsciiString();


				if (sendingGcode) {
					if (eventString.contains("ok")){
						//						okRecived = true;
						//						System.out.println("Ok recieved: " + ok);
						outputs.append("\nOk recieved: " + eventString);

						synchronized(monitor) {
							monitor.notifyAll();
						}
					}else {
						//						System.out.println("!Other than ok!!: " + ok);
						//						output.append("\n!!Other than ok!!: " + ok);
						outputs.append("\"\\n!!Other than ok!!:  " + eventString);
					}
				}


			}catch(Exception ex) {
				ex.printStackTrace();
			}

		}

	}
}
