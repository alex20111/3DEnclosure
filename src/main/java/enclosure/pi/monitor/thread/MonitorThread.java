package enclosure.pi.monitor.thread;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import enclosure.pi.monitor.arduino.ArduinoAllSensorsData;
import enclosure.pi.monitor.arduino.Lights.LightAction;
import enclosure.pi.monitor.common.Constants;
import enclosure.pi.monitor.common.SensorsData;
import enclosure.pi.monitor.common.SharedData;
import enclosure.pi.monitor.db.entity.Config;


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
		
//		File dataFile = new File("/opt/jetty/dataCsv.txt");
//		BufferedWriter writer = null;
//		try {
//			writer = new BufferedWriter(new FileWriter(dataFile));
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

		SharedData sd = SharedData.getInstance(); 
		sd.putSensor(SensorsData.EXTR_SPEED, 0); //init value that we know that it ill be 0 at start.
		sd.putSensor(SensorsData.LIGHT_STATUS, LightAction.OFF); 
		
		while(keepMonitoring) {
			//get all sensor information
			try {
				ArduinoAllSensorsData data = new ArduinoAllSensorsData();
				data.requestAllSensorInfo();
				
//				String writeTofile = LocalDateTime.now().toString() + "," +sd.getSensorAsString(SensorsData.AIR_VOC) + "," + sd.getSensorAsString(SensorsData.AIR_CO2) + "\n";
//				writer.write(writeTofile);
//				
//				writer.flush();
				
				Config cfg = (Config)sd.getSharedObject(Constants.CONFIG);
				
				if (cfg.isExtractorAuto()) {
					
				}
				
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
