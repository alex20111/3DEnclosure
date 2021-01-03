package enclosure.pi.monitor.arduino;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

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


public class ArduinoHandler {

	private static final Logger logger = LogManager.getLogger(ArduinoHandler.class);

	private Serial serial;
	private SerialConfig config;
	private boolean arduinoReady = false;

	private static ArduinoHandler arduinoHandler;
	private final BlockingQueue<Integer> rpmQueue =  new ArrayBlockingQueue<>(1);
	private final BlockingQueue<String> encTemp =  new ArrayBlockingQueue<>(1);
	private final BlockingQueue<String> allSensorQueue =  new ArrayBlockingQueue<>(1);

	private ArduinoSerialEvent ardSerialEvent;

	private ArduinoHandler() {		
		serial = SerialFactory.createInstance();
		config = new SerialConfig();
		config.device("/dev/ttyUSB0")
		.baud(Baud._19200)
		.dataBits(DataBits._8)
		.parity(Parity.NONE)
		.stopBits(StopBits._1)
		.flowControl(FlowControl.NONE);

		startListener();

		ardSerialEvent = new ArduinoSerialEvent();

	}

	public static ArduinoHandler getInstance() {
		if (arduinoHandler == null) {
			synchronized (ArduinoHandler.class) {
				if(arduinoHandler == null) {
					logger.info( "arduinoHandler initialized");
					arduinoHandler = new ArduinoHandler();
				}
			}
		}
		return arduinoHandler;
	}	

	public void openSerialConnection() throws IOException {
		if (!serial.isOpen()) {
			serial.open(config);
		}
	}

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
			serial.write(command);
		}else {
			logger.info("Arduino not ready.. bypassing command");
		}

	}

	//TODO  just added start and end markers.. needs to check for it ..

	private void startListener() {
		serial.addListener(new SerialDataEventListener() {
			@Override
			public void dataReceived(SerialDataEvent event) {
				try {
					String eventString =  event.getAsciiString();

					if ("ready".equalsIgnoreCase(eventString)) {
						arduinoReady = true;
						logger.info("!! Arduino READY !!");
					} else { 
						ardSerialEvent.translateReceivedEvent(eventString);

						if (ardSerialEvent.isCommandComplete()) {
							if (ardSerialEvent.isCommand(Command.GET_RPM_CMD)) {
								rpmQueue.offer( Integer.parseInt( ardSerialEvent.getOutput() ), 4000, TimeUnit.MILLISECONDS);
							}else if (ardSerialEvent.isCommand(Command.TEMPERATURE_CMD)) {
								encTemp.offer(ardSerialEvent.getOutput(), 4000, TimeUnit.MILLISECONDS) ;
							}else if(ardSerialEvent.isCommand(Command.ALL_SESNORS))  {
								allSensorQueue.offer(ardSerialEvent.getOutput(), 4000, TimeUnit.MILLISECONDS);
							}
						}				
					}

				} catch (Exception e) {
					logger.error("Error in arduino listener" , e);
				} 
			}
		});
	}
}
