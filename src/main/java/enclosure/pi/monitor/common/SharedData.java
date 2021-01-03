package enclosure.pi.monitor.common;

import java.util.Map;
import java.util.WeakHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import enclosure.pi.monitor.arduino.ArduinoAllSensorsData;
import enclosure.pi.monitor.arduino.Lights.LightAction;

public class SharedData {
	
	private static final Logger logger = LogManager.getLogger(SharedData.class);
	
	private static SharedData sharedData;
	
	private Map<SensorsData, Object> sensorsData = new WeakHashMap<>();
	
//	private int fanSpeed = -1;
//	private LightAction lightStatus;
//	private ArduinoAllSensorsData arduinoAllSensorData;
	
	//TODO: config class here
	
	
	public static SharedData getInstance() {
		if (sharedData == null) {
			synchronized (SharedData.class) {
				if(sharedData == null) {
					logger.info( "SharedData initialized");
					sharedData = new SharedData();
				}
			}
		}
		return sharedData;
	}	
	public void putSensor(SensorsData sensor, Object value) {
		this.sensorsData.put(sensor, value);
	}
	
	public String getSensorAsString(SensorsData sensor) {
		return (String)this.sensorsData.get(sensor);
	}
	
	public int getSensorAsInt(SensorsData sensor) {
		return (int)this.sensorsData.get(sensor);
	}
	public Object getSensor(SensorsData sensor) {
		return this.sensorsData.get(sensor);
	}
	
//	public int getFanSpeed() {
//		return fanSpeed;
//	}
//	public void setFanSpeed(int speed) {
//		this.fanSpeed = speed;
//	}	
//	public void setLightStatus(LightAction status) {
//		this.lightStatus = status;
//	}
//	public LightAction getLightStatus() {
//		return this.lightStatus;
//	}
//
//	public ArduinoAllSensorsData getArduinoAllSensorData() {
//		return arduinoAllSensorData;
//	}
//
//	public void setArduinoAllSensorData(ArduinoAllSensorsData arduinoAllSensorData) {
//		this.arduinoAllSensorData = arduinoAllSensorData;
//	}

	
	
//	public Map<SensorsData, Object> getSensorsData() {
//		return sensorsData;
//	}
//
//	public void setSensorsData(Map<SensorsData, Object> sensorsData) {
//		this.sensorsData = sensorsData;
//	}

}
