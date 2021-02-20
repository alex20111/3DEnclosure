package enclosure.pi.monitor.arduino;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//
//import com.pi4j.io.serial.Baud;
//import com.pi4j.io.serial.DataBits;
//import com.pi4j.io.serial.FlowControl;
//import com.pi4j.io.serial.Parity;
//import com.pi4j.io.serial.Serial;
//import com.pi4j.io.serial.SerialConfig;
//import com.pi4j.io.serial.SerialDataEvent;
//import com.pi4j.io.serial.SerialDataEventListener;
//import com.pi4j.io.serial.SerialFactory;
//import com.pi4j.io.serial.StopBits;


public class ArduinoHandlerBkup {

	private static final Logger logger = LogManager.getLogger(ArduinoHandlerBkup.class);

//	private Serial serial;
//	private SerialConfig config;
	private boolean arduinoReady = false;

	private static ArduinoHandlerBkup arduinoHandler;
	private final BlockingQueue<Integer> rpmQueue =  new ArrayBlockingQueue<>(1);
	private final BlockingQueue<String> encTemp =  new ArrayBlockingQueue<>(1);
	private final BlockingQueue<String> allSensorQueue =  new ArrayBlockingQueue<>(1);

	private ArduinoSerialEvent ardSerialEvent;

//	private ArduinoHandlerBkup() {		
//		
//		String usbPort = "/dev/serial/by-path/platform-3f980000.usb-usb-0:1.4.2:1.0-port0";
//		
//		serial = SerialFactory.createInstance();
//		config = new SerialConfig();
//		config.device(usbPort)
//		.baud(Baud._19200)
//		.dataBits(DataBits._8)
//		.parity(Parity.NONE)
//		.stopBits(StopBits._1)
//		.flowControl(FlowControl.NONE);
//
//		startListener();
//
//		ardSerialEvent = new ArduinoSerialEvent();
//
//	}

	public static ArduinoHandlerBkup getInstance() {
		if (arduinoHandler == null) {
			synchronized (ArduinoHandlerBkup.class) {
				if(arduinoHandler == null) {
					logger.info( "arduinoHandler initialized");
					arduinoHandler = new ArduinoHandlerBkup();
				}
			}
		}
		return arduinoHandler;
	}	

//	public void openSerialConnection() throws IOException {
//		if (!serial.isOpen()) {
//			serial.open(config);
//		}
//	}

	public BlockingQueue<Integer> extFanRpmQueue(){
		return rpmQueue;
	}

	public BlockingQueue<String> getEncTempQueue(){
		return encTemp;
	}

	public BlockingQueue<String> getAllSensorQueue() {
		return allSensorQueue;
	}

	public boolean isArduinoReady() {
		return arduinoReady;
	}

	public synchronized void writeToSerial(String command) throws IllegalStateException, IOException {
		logger.debug("Arduino Ready? " + arduinoReady + " Writing command: " + command);

		if (arduinoReady) {
//			serial.write(command);
		}else {
			logger.info("Arduino not ready.. bypassing command");
		}

	}

	private void startListener() {
//		serial.addListener(new SerialDataEventListener() {
//			@Override
//			public void dataReceived(SerialDataEvent event) {
//				try {
//					String eventString =  event.getAsciiString();
//	
//					if ("Ready".equalsIgnoreCase(eventString.trim())) {
//						arduinoReady = true;
//						logger.info("!! Arduino READY !!");
//					} else { 
//						
//						ardSerialEvent.processEvent(eventString);
//						
//						List<CleanedEvent> ev = ardSerialEvent.getOutputs();
//						
//						logger.debug("eventString: " + eventString + " Returned event -------------------> " + ev);
//						
//						for(CleanedEvent cl : ev) {
//							if (cl.getCommand().equalsIgnoreCase(Command.GET_RPM_CMD)) {
//								rpmQueue.offer( Integer.parseInt( cl.getData() ), 4000, TimeUnit.MILLISECONDS);
//							}else if (cl.getCommand().equalsIgnoreCase(Command.TEMPERATURE_CMD)) {
//								encTemp.offer(cl.getData(), 4000, TimeUnit.MILLISECONDS) ;
//							}else if(cl.getCommand().equalsIgnoreCase(Command.ALL_SESNORS))  {
//								allSensorQueue.offer(cl.getData(), 4000, TimeUnit.MILLISECONDS);
//							}
//						}
//		
//					}
//
//				} catch (Exception e) {
//					logger.error("Error in arduino listener" , e);
//				} 
//			}
//		});
	}
}
