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
import enclosure.pi.monitor.printer.PrinterHandler;


//add data here to monitor automatically if function is enable.
//if auto monitor is not switched on, then it just gather data,
public class MonitorThread implements Runnable{

	private static final Logger logger = LogManager.getLogger(MonitorThread.class);

	private int delay = 1000;
	private boolean keepMonitoring = true;

	//smoke variables
	private int smokeSensorLimit = 500;
	private int smokeCo2Limt = 30000;
	private int smokePPMLimit = 10000;
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
		
		boolean printStarted =  PrinterHandler.getInstance().isPrinting();	

		if (printStarted) {
			
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
			int currentPPmLimit = getVoc(sd);
		
			float tempLimit = (float)cfg.getEncTempLimit();
			float currentTemp = getCurrTemp(sd);	
	
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
				 fan.decreaseSpeed();
			}
		}else if (!printStarted) {			
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
	private void processFireAlarm(SharedData sd) throws IllegalStateException, IOException {

		logger.debug("processFireAlarm");

		//check sensors and if alarm is set, send it.. if fire..shut down ventilation... send alarm in 3 steps 1st one warning possible.. 30 sec after re-confirm.
		int smokeSensor = getMq2Sensor(sd);
		int co2 = getCurrCO2(sd);
		int vocPPM = getVoc(sd);


		if (  smokeSensor > smokeSensorLimit || 
				co2 > smokeCo2Limt || 
				vocPPM > smokePPMLimit ) {						
			//send 1st step warning. 
			if (smokeLevel == SmokeLevel.NO_SMOKE) {
				smokeLevel = SmokeLevel.WARNING;
				//then wait 45 seconds before escalating if there is still smoke.
				logger.info("sent SMOKE warning alarm");
				nextFireAlarmToBeSent =  LocalDateTime.now().plusSeconds(45);
				sendSMS("!!!!WARNING!!! ", "Smoke detected.\nCO2: " + co2 + "\nVOC: " + vocPPM + "\nMq2: " + smokeSensor);
			}else if (smokeLevel == SmokeLevel.WARNING) {
				//check if the 30 seconds is up.. if it's up, then send severe warning
				LocalDateTime now = LocalDateTime.now();
				if (now.isAfter(nextFireAlarmToBeSent)) {
					logger.info("sent smoke ALARM!!!!!! m");
					smokeLevel = SmokeLevel.ALARM;
					nextFireAlarmToBeSent = LocalDateTime.now().plusMinutes(5);
					sendSMS("!!!!ALARM!!! ", "You got SMOKE!!! .\nCO2: " + co2 + "\nVOC: " + vocPPM + "\nMq2: " + smokeSensor);
					//stop fan
					ExtractorFan fan = new ExtractorFan(ExtractorFanCmd.SET_SPEED);
					fan.setFanSpeed(0);
				}
			}else if (smokeLevel == SmokeLevel.ALARM) {
				LocalDateTime now = LocalDateTime.now();
				if(now.isAfter(nextFireAlarmToBeSent)) {
					nextFireAlarmToBeSent = LocalDateTime.now().plusMinutes(5);
					logger.info("sent smoke ALARM!!!!!! reminder");
					sendSMS("!!!!ALARM REMINDER!!! ", "You got SMOKE!!! Next remonder in 5 min .\nCO2: " + co2 + "\nVOC: " + vocPPM + "\nMq2: " + smokeSensor);
				}
			}

		}else {
			smokeLevel = SmokeLevel.NO_SMOKE;
			nextFireAlarmToBeSent = null;
		}
	}
	
	private int getVoc(SharedData sd) {
		int currentPPmLimit  = -1;
		try {
			 currentPPmLimit = Integer.parseInt(sd.getSensorAsString(SensorsData.AIR_VOC));
		}catch(NumberFormatException f ) {
			logger.info("ENCLOUSRE currentPPmLimit is not found or cannot be converted. Value: " + sd.getSensorAsString(SensorsData.AIR_VOC) );
		}
		return currentPPmLimit;
	}

	private float getCurrTemp(SharedData sd) {
		float currTmp = 0.0f;
		try {
			currTmp = Float.parseFloat(sd.getSensorAsString(SensorsData.ENC_TEMP));
		}catch(NumberFormatException nfx) {
			logger.info("ENCLOUSRE temp is not found or cannot be converted. Value: " + sd.getSensorAsString(SensorsData.ENC_TEMP) );
		}
		
		return currTmp;
	}
	
	private int getCurrCO2(SharedData sd){
		int currCo2 = -1;
		try {
			currCo2 = Integer.parseInt(sd.getSensorAsString(SensorsData.AIR_CO2));
		}catch(NumberFormatException f ) {
			logger.info("ENCLOUSRE CO2 is not found or cannot be converted. Value: " + sd.getSensorAsString(SensorsData.AIR_CO2) );
		}
		return currCo2;
	}
	private int getMq2Sensor(SharedData sd) {
		int mq2 = -1;
		try {
			mq2 = Integer.parseInt(sd.getSensorAsString(SensorsData.MQ2));
		}catch(NumberFormatException f ) {
			logger.info("MQ2 Sensor info is not found or cannot be converted. Value: " + sd.getSensorAsString(SensorsData.MQ2) );
		}
		return mq2;
	}
	private void sendSMS(String subject, String message) {
		SendSMSThread sms = new SendSMSThread(subject, message);

		ThreadManager.getInstance().sendSmsMessage(sms);
	}
	
	
	public void stopMonitoring() {
		this.keepMonitoring = false;
	}

	enum SmokeLevel{
		WARNING, ALARM, NO_SMOKE;
	}
}
