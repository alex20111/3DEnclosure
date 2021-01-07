package enclosure.pi.monitor.db.entity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Config {

	public static final String TBL_NAME 	= "config";
	public static final String ID 			= "id";
	public static final String LIGHTS_ON 	= "lights_on";
	public static final String EXTR_AUTO 	= "extractor_auto";
	public static final String EXTR_PPM_LIMIT = "extractor_ppm_limit";
	public static final String ENC_TEMP_LIMIT = "enclosure_temp_limit";
	public static final String FIRE_ALARM 	= "fire_alarm_auto";
	public static final String SMS_PHONE 	= "sms_phone_number";	
	
	private int id = -1;
	
	private boolean lightsOn    	= false;	
	private boolean extractorAuto         = false;
	private int extrPPMLimit		= -1;
	private int encTempLimit		= -1;
	private boolean fireAlarmAuto 	= false;
	private String smsPhoneNumber 	= "";
	
	public Config() {}
	public Config(ResultSet rs) throws SQLException {
		this.id = rs.getInt(ID);
		this.lightsOn = rs.getBoolean(LIGHTS_ON);		
		this.extractorAuto = rs.getBoolean(EXTR_AUTO);
		this.extrPPMLimit = rs.getInt(EXTR_PPM_LIMIT);
		this.encTempLimit = rs.getInt(ENC_TEMP_LIMIT);
		this.fireAlarmAuto = rs.getBoolean(FIRE_ALARM);
		this.smsPhoneNumber = rs.getString(SMS_PHONE);
		
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public boolean isLightsOn() {
		return lightsOn;
	}
	public void setLightsOn(boolean lightsOn) {
		this.lightsOn = lightsOn;
	}
	public boolean isExtractorAuto() {
		return extractorAuto;
	}
	public void setExtractorAuto(boolean extractorAuto) {
		this.extractorAuto = extractorAuto;
	}
	public int getExtrPPMLimit() {
		return extrPPMLimit;
	}
	public void setExtrPPMLimit(int extrPPMLimit) {
		this.extrPPMLimit = extrPPMLimit;
	}
	public int getEncTempLimit() {
		return encTempLimit;
	}
	public void setEncTempLimit(int encTempLimit) {
		this.encTempLimit = encTempLimit;
	}
	public boolean isFireAlarmAuto() {
		return fireAlarmAuto;
	}
	public void setFireAlarmAuto(boolean fireAlarmAuto) {
		this.fireAlarmAuto = fireAlarmAuto;
	}
	public String getSmsPhoneNumber() {
		return smsPhoneNumber;
	}
	public void setSmsPhoneNumber(String smsPhoneNumber) {
		this.smsPhoneNumber = smsPhoneNumber;
	}
	
	
}
