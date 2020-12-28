package enclosure.pi.monitor.arduino;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

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

import enclosure.pi.monitor.arduino.ExtractorFan.ExtractorFanCmd;

public class ArduinoHandler {

	private static final Logger logger = LogManager.getLogger(ArduinoHandler.class);

	private Serial serial;
	private SerialConfig config;

	private static ArduinoHandler arduinoHandler;
	private final BlockingQueue<Integer> rpmQueue =  new ArrayBlockingQueue<>(1);
	private final BlockingQueue<String> encTemp =  new ArrayBlockingQueue<>(1);

	private ArduinoHandler() {		
		serial = SerialFactory.createInstance();
		config = new SerialConfig();
		config.device("/dev/ttyUSB0")
		.baud(Baud._9600)
		.dataBits(DataBits._8)
		.parity(Parity.NONE)
		.stopBits(StopBits._1)
		.flowControl(FlowControl.NONE);

		startListener();

		try {
			openSerialConnection();
		} catch (IOException e) {
			logger.error("error in ard constructor", e);
		}
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
	

	//	public int getFanRpm() {
	//		logger.debug("Get fan RPM");
	//		int rpmVal = -1;
	//		writeToSerial("<r>");
	//		try{
	//			rpmVal = rpmQueue.poll(4000, TimeUnit.MILLISECONDS); 
	//
	//		}catch(Exception ex) {
	//			logger.error("error in fan rpm", ex);
	//		}
	//		return rpmVal;
	//	}
	//	
	//	public void setFanSpeed (int speed) throws IllegalStateException, IOException {
	//		serial.write("<s" + speed + ">");
	//	}
	//	
	//	public 
	//	
	//	public void sendLightCommand(Lights light) {
	//		
	//	}



	public synchronized void writeToSerial(String command) throws IllegalStateException, IOException {
		logger.debug("Arduino writing command: " + command);

		serial.write(command);


	}

	private void startListener() {
		serial.addListener(new SerialDataEventListener() {
			@Override
			public void dataReceived(SerialDataEvent event) {
				try {
					String serialEvent =  event.getAsciiString();
					logger.debug("startListener Data: " +  serialEvent);
					

					if ("ready".equals(serialEvent)) {
						logger.info("Arduino READY!");
					}else if (serialEvent.trim().startsWith(ExtractorFanCmd.GET_RPM.getCmdStr()) ){
						rpmQueue.put(Integer.parseInt(serialEvent.substring(1,serialEvent.trim().length())));
					}else if(serialEvent.trim().startsWith(TemperatureEnclosure.TEMP_COMMAND)){
						encTemp.put(serialEvent.substring(1,serialEvent.trim().length())) ;
					}
				} catch (Exception e) {
					logger.error("Error in arduino listener" , e);
				} 
			}
		});
	}
}
