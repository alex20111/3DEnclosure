package enclosure.pi.monitor.thread;


import java.io.IOException;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import enclosure.pi.monitor.arduino.ArduinoAllSensorsData;
import enclosure.pi.monitor.arduino.ExtractorFan;
import enclosure.pi.monitor.arduino.Lights;
import enclosure.pi.monitor.arduino.ExtractorFan.ExtractorFanCmd;
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

	private int smokeSensorLimit = 500;
	private SmokeLevel smokeLevel = SmokeLevel.NO_SMOKE;
	private LocalDateTime nextFireAlarmToBeSent;      //the 1st one is sent automatically.. the next one each 5 min..

	//extractor
	LocalDateTime nextSpeedIncrease = null;
	LocalDateTime stopFanTimer = null;
	boolean autoStarted = false;


	public MonitorThread(int delay) {
		logger.debug("Init monitorThread with delay: " + delay );
		this.delay = delay;
	}

	@Override
	public void run() {

		SharedData sd = SharedData.getInstance(); 
		Config cfg = (Config)sd.getSharedObject(Constants.CONFIG);

		try {

			sd.putSensor(SensorsData.EXTR_SPEED, 0); //init value that we know that it ill be 0 at start.
			sd.putSensor(SensorsData.LIGHT_STATUS, LightAction.OFF);

			//do initialization
			if (cfg.isLightsOn()) {
				logger.info("Auto turning lights");
				Lights l = new Lights(LightAction.ON);
				l.triggerLight();
			}

		}catch(Exception ex) {
			logger.error("exception in initialization of arduino config variables." ,ex);
		}

		while(keepMonitoring) {
			//get all sensor information
			try {
				ArduinoAllSensorsData data = new ArduinoAllSensorsData();
				data.requestAllSensorInfo();

				cfg = (Config)sd.getSharedObject(Constants.CONFIG);

				if (cfg.isExtractorAuto()) {
					processExtractorControl(sd, cfg);
				}
				if (cfg.isFireAlarmAuto()) {
					processFireAlarm(sd);					
				}

				Thread.sleep(delay);
			} catch (Exception e) {
				logger.error("error in monitorthread. " , e);
				this.keepMonitoring = false;
			}

		}
	}
	private void processExtractorControl(SharedData sd, Config cfg) {
		
		Boolean printStarted = (Boolean)sd.getSharedObject(Constants.PRINT_STARTED);
		

		if (printStarted != null && printStarted.booleanValue()) {
			
			if (!autoStarted) {
				logger.debug("Auto start fan");
				autoStarted = true;
				ExtractorFan fan = new ExtractorFan(ExtractorFanCmd.SET_SPEED);
				try {
					fan.setFanSpeed(10);
				} catch (IllegalStateException | IOException e) {
					logger.error(e);
				};
			}

			//verify with VOC to see if it's too high.. if too high.. turn fan on..
			boolean increaseFanOutput = false;
	
			int extPPmlimit = cfg.getExtrPPMLimit();
			int currentPPmLimit = 0;
			try {
				currentPPmLimit = Integer.parseInt(sd.getSensorAsString(SensorsData.AIR_VOC));
			}catch(NumberFormatException f ) {
				logger.info("ENCLOUSRE currentPPmLimit is not found or cannot be converted. Value: " + sd.getSensorAsString(SensorsData.AIR_VOC) );
			}
			float tempLimit = (float)cfg.getEncTempLimit();
			float currentTemp = 0.0f;
			try {
				currentTemp = Float.parseFloat(sd.getSensorAsString(SensorsData.ENC_TEMP));
			}catch(NumberFormatException nfx) {
				logger.info("ENCLOUSRE temp is not found or cannot be converted. Value: " + sd.getSensorAsString(SensorsData.ENC_TEMP) );
			}
	
	
			if (currentPPmLimit > extPPmlimit || currentTemp >  tempLimit) {
				increaseFanOutput = true;
			}
	
			if (increaseFanOutput) {
	
				if (nextSpeedIncrease == null || LocalDateTime.now().isAfter(nextSpeedIncrease)) {				
					ExtractorFan fan = new ExtractorFan(ExtractorFanCmd.SET_SPEED);
					boolean maxSpeed = fan.increaseSpeed();
					if (!maxSpeed) {
						logger.debug("Increasing fan speed because temp or PPM are too high. currentPPmLimit: " + currentPPmLimit + " currentTemp: " + currentTemp);
						nextSpeedIncrease = LocalDateTime.now().plusMinutes(1);
					}
				}
			}else if (nextSpeedIncrease != null) {
				//this means that we increased the fan speed and then the ppm went below the limit..
				//decrease it by 10% and maintain it there until required.			
				logger.debug("Resetting  fan speed because - 10 % ");
				nextSpeedIncrease = null;
				ExtractorFan fan = new ExtractorFan(ExtractorFanCmd.SET_SPEED);
				boolean stopped = fan.decreaseSpeed();
			}
		}else if (printStarted != null && !printStarted.booleanValue()) {			
			//stop the fan after 1 minute
			if (stopFanTimer == null && autoStarted) {
				stopFanTimer = LocalDateTime.now().plusMinutes(5);
			}
			else if (stopFanTimer != null && LocalDateTime.now().isAfter(stopFanTimer) && autoStarted) {
				logger.debug("Printing stopped and stopping fan");
				autoStarted = false;
				ExtractorFan fan = new ExtractorFan(ExtractorFanCmd.SET_SPEED);
				try {
					fan.setFanSpeed(0);
					stopFanTimer = null;
				} catch (IllegalStateException | IOException e) {
					logger.error(e);
				};
			}
		}


	}
	private void processFireAlarm(SharedData sd) {

		logger.debug("processFireAlarm");

		//check sensors and if alarm is set, send it.. if fire..shut down ventilation... send alarm in 3 steps 1st one warning possible.. 30 sec after re-confirm.
		int smokeSensor = 0;
		try {
			smokeSensor = Integer.parseInt(sd.getSensorAsString(SensorsData.MQ2));

		}catch(NumberFormatException f) {
			logger.error("processFireAlarm ,value error" , f);
		}

		if (smokeSensor > smokeSensorLimit) {						
			//send 1st step warning. 
			if (smokeLevel == SmokeLevel.NO_SMOKE) {
				smokeLevel = SmokeLevel.WARNING;
				//then wait 30 seconds before escalating if there is still smoke.
				//TODO send warning message with flame sensor data.. 
				logger.info("sent SMOKE warning alarm");
				nextFireAlarmToBeSent =  LocalDateTime.now().plusSeconds(30);
			}else if (smokeLevel == SmokeLevel.WARNING) {
				//check if the 30 seconds is up.. if it's up, then send severe warning
				LocalDateTime now = LocalDateTime.now();
				if (now.isAfter(nextFireAlarmToBeSent)) {
					//TODO send other alarm reminder.. --- SHUT down ventilation
					logger.info("sent smoke ALARM!!!!!! m");
					smokeLevel = SmokeLevel.ALARM;
					nextFireAlarmToBeSent = LocalDateTime.now().plusMinutes(5);
				}
			}else if (smokeLevel == SmokeLevel.ALARM) {
				LocalDateTime now = LocalDateTime.now();
				if(now.isAfter(nextFireAlarmToBeSent)) {
					nextFireAlarmToBeSent = LocalDateTime.now().plusMinutes(5);
					logger.info("sent smoke ALARM!!!!!! reminder");
					//TODO send other alarm reminder.. 
				}
			}

		}else {
			smokeLevel = SmokeLevel.NO_SMOKE;
			nextFireAlarmToBeSent = null;
		}
	}

	public void stopMonitoring() {
		this.keepMonitoring = false;
	}

	enum SmokeLevel{
		WARNING, ALARM, NO_SMOKE;
	}
}
