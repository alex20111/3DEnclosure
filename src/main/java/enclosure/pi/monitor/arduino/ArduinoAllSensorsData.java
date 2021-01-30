package enclosure.pi.monitor.arduino;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import enclosure.pi.monitor.common.SensorsData;
import enclosure.pi.monitor.common.SharedData;

//class containing all the sensor data from the arduino

public class ArduinoAllSensorsData implements Command{
	
	private static final Logger logger = LogManager.getLogger(ArduinoAllSensorsData.class);
	
private ArduinoHandler ah;
private SharedData sd;

	
	public ArduinoAllSensorsData() {
		ah  = ArduinoHandler.getInstance();
		sd = SharedData.getInstance();
	}
	
	public void requestAllSensorInfo() throws IllegalStateException, IOException, InterruptedException {
		StringBuilder sb = new StringBuilder();
		sb.append(START_MARKER);
		sb.append(ALL_SESNORS);
		sb.append(END_MARKER);
		ah.writeToSerial(sb.toString());
		
		String temp = ah.getAllSensorQueue().poll(4000, TimeUnit.MILLISECONDS);
		
		if (temp != null && temp.length() > 0) {
			String[] split = temp.split("-");
			
			sd.putSensor(SensorsData.EXTR_RPM, Integer.parseInt(split[0]));
			sd.putSensor(SensorsData.ENC_TEMP, split[1]);
			sd.putSensor(SensorsData.AIR_CO2, split[2]);
			sd.putSensor(SensorsData.AIR_VOC, split[3]);
			sd.putSensor(SensorsData.MQ2, split[4]);
			sd.putSensor(SensorsData.FLAME1, split[5]);
			sd.putSensor(SensorsData.FLAME2, split[6]);

		}
		
//		return temp;
	}

}
