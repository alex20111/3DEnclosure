package enclosure.pi.monitor.service.model;

public class DashBoard {
	private boolean extrFanOnAuto = false;
	private int extracFanRPM = -1;
	private int extracFanSpeed = -1;
	private String temperature = "";
	private boolean lightOn = false;
	private String airQualityCo2 = "";
	private String airQualityVoc = "";
	
	public boolean isExtrFanOnAuto() {
		return extrFanOnAuto;
	}
	public void setExtrFanOnAuto(boolean extrFanOnAuto) {
		this.extrFanOnAuto = extrFanOnAuto;
	}
	public int getExtracFanRPM() {
		return extracFanRPM;
	}
	public void setExtracFanRPM(int extracFanRPM) {
		this.extracFanRPM = extracFanRPM;
	}
	public int getExtracFanSpeed() {
		return extracFanSpeed;
	}
	public void setExtracFanSpeed(int extracFanSpeed) {
		this.extracFanSpeed = extracFanSpeed;
	}
	public String getTemperature() {
		return temperature;
	}
	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}
	public boolean isLightOn() {
		return lightOn;
	}
	public void setLightOn(boolean lightOn) {
		this.lightOn = lightOn;
	}
	public String getAirQualityVoc() {
		return airQualityVoc;
	}
	public void setAirQualityVoc(String airQualityVoc) {
		this.airQualityVoc = airQualityVoc;
	}
	public String getAirQualityCo2() {
		return airQualityCo2;
	}
	public void setAirQualityCo2(String airQualityCo2) {
		this.airQualityCo2 = airQualityCo2;
	}
	

}
