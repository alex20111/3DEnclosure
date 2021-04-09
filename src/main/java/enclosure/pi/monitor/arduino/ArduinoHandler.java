package enclosure.pi.monitor.arduino;


import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;



public class ArduinoHandler {

	private static final Logger logger = LogManager.getLogger(ArduinoHandler.class);

	//	private Serial serial;
	//	private SerialConfig config;
	private SerialPort arduinoPort;
	private boolean arduinoPortOpen = false;
	private boolean arduinoReady = false;

	private static ArduinoHandler arduinoHandler;
	private final BlockingQueue<Integer> rpmQueue =  new ArrayBlockingQueue<>(1);
	private final BlockingQueue<String> encTemp =  new ArrayBlockingQueue<>(1);
	private final BlockingQueue<String> allSensorQueue =  new ArrayBlockingQueue<>(1);

	private ArduinoSerialEvent ardSerialEvent;

	private ArduinoHandler() {		

		boolean portOpen = connectToArduino();		

		if (portOpen) {
			arduinoPortOpen = true;
		}


		startListener();
		//
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

		if (arduinoReady && arduinoPortOpen) {
			byte[] toB = command.getBytes();
			arduinoPort.writeBytes(toB, toB.length);
		}else {
			logger.info("Arduino not ready.. bypassing command");
		}

	}

	private void startListener() {

		arduinoPort.addDataListener(new SerialPortDataListener() {
			@Override
			public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_RECEIVED; }

			@Override
			public void serialEvent(SerialPortEvent event)
			{

				try {
					StringBuilder eventString = new StringBuilder();
					byte[] newData = event.getReceivedData();
					//							System.out.println("Received data of size: " + newData.length);
					for (int i = 0; i < newData.length; ++i)	{
						eventString.append((char)newData[i]);

					}

					String evnt = eventString.toString().trim();

					//							logger.debug("From arduino: " + evnt);
					if ("Ready".equalsIgnoreCase(evnt)) {
						arduinoReady = true;
						logger.info("!! Arduino READY !!");
					} else { 

						ardSerialEvent.processEvent(evnt);

						List<CleanedEvent> ev = ardSerialEvent.getOutputs();

						logger.debug("eventString: " + eventString + " Returned event -------------------> " + ev);

						for(CleanedEvent cl : ev) {
							if (cl.getCommand().equalsIgnoreCase(Command.GET_RPM_CMD)) {
								rpmQueue.offer( Integer.parseInt( cl.getData() ), 4000, TimeUnit.MILLISECONDS);
							}else if (cl.getCommand().equalsIgnoreCase(Command.TEMPERATURE_CMD)) {
								encTemp.offer(cl.getData(), 4000, TimeUnit.MILLISECONDS) ;
							}else if(cl.getCommand().equalsIgnoreCase(Command.ALL_SESNORS))  {
								allSensorQueue.offer(cl.getData(), 4000, TimeUnit.MILLISECONDS);
							}
						}

					}

				}catch(Exception ex) {
					logger.error("Serial event" , ex);
				}
			}
		});

	}

	private boolean connectToArduino() {
		String usbPort = "/dev/serial/by-path/platform-3f980000.usb-usb-0:1.3.2:1.0-port0";


		arduinoPort = SerialPort.getCommPort(usbPort);

		arduinoPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
		arduinoPort.setBaudRate(19200);
		arduinoPort.setNumDataBits(8);
		arduinoPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
		arduinoPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
		arduinoPort.setParity(SerialPort.NO_PARITY);

		boolean portOpen = arduinoPort.openPort();

		if (portOpen) {
			return true;
		}

		return false;
	}
}
