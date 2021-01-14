package enclosure.pi.monitor.service.model;

/**
 * 
 * @author ADMIN
 *
 */
public class ExtrFanParam {
	private int fanSpeed = -1;
	private boolean fanIsOnAuto = false;
	
	public ExtrFanParam() {}
	
	public ExtrFanParam(int fanSpeed, boolean fanAuto) {
		this.fanSpeed = fanSpeed;
		this.fanIsOnAuto = fanAuto;
	}
	
	
	public int getFanSpeed() {
		return fanSpeed;
	}
	public void setFanSpeed(int fanSpeed) {
		this.fanSpeed = fanSpeed;
	}
	public boolean isFanIsOnAuto() {
		return fanIsOnAuto;
	}
	public void setFanIsOnAuto(boolean fanIsOnAuto) {
		this.fanIsOnAuto = fanIsOnAuto;
	}
}
