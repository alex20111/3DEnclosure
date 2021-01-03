package enclosure.pi.monitor.thread;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import enclosure.pi.monitor.arduino.ArduinoAllSensorsData;
import enclosure.pi.monitor.arduino.Lights.LightAction;
import enclosure.pi.monitor.common.SensorsData;
import enclosure.pi.monitor.common.SharedData;


//add data here to monitor automatically if function is enable.
//if auto monitor is not switched on, then it just gather data,
public class MonitorThread implements Runnable{

	private static final Logger logger = LogManager.getLogger(MonitorThread.class);

	private int delay = 1000;
	private boolean keepMonitoring = true;

	public MonitorThread(int delay) {
		logger.debug("Init monitorThread with delay: " + delay );
		this.delay = delay;
	}

	@Override
	public void run() {

		SharedData sd = SharedData.getInstance(); 
		sd.putSensor(SensorsData.EXTR_SPEED, 0); //init value that we know that it ill be 0 at start.
		sd.putSensor(SensorsData.LIGHT_STATUS, LightAction.OFF); 
		
		while(keepMonitoring) {
			//get all sensor information
			try {
				ArduinoAllSensorsData data = new ArduinoAllSensorsData();
				data.requestAllSensorInfo();
				
				
				
				Thread.sleep(delay);
			} catch (Exception e) {
				logger.error("error in monitorthread. " , e);
				this.keepMonitoring = false;
			}

		}
	}

	public void stopMonitoring() {
		this.keepMonitoring = false;
	}

}
