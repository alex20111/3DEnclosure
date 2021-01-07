package enclosure.pi.monitor.arduino;

import java.io.IOException;

import enclosure.pi.monitor.common.SensorsData;
import enclosure.pi.monitor.common.SharedData;


public class Lights implements Command{
	

	private LightAction lightAction;
	private ArduinoHandler ah;

	public Lights(LightAction action) {
		this.lightAction = action;
		this.ah = ArduinoHandler.getInstance();
	}


	public void triggerLight() throws IllegalStateException, IOException {
		StringBuilder sb = new StringBuilder();
		sb.append(START_MARKER);
		sb.append(LIGHTS_CMD);
		sb.append(this.lightAction.getAction());		
		sb.append(END_MARKER);
		ah.writeToSerial(sb.toString());
		
		SharedData.getInstance().putSensor(SensorsData.LIGHT_STATUS, getLightStatus());
	}
	public LightAction getLightStatus() 
	{
		return lightAction;
	}
	
	
	public enum LightAction{
		ON(1), OFF(0);
		
		private int action = -1;
		
		private LightAction (int action) {
			this.action = action;
		}
		
		public int getAction() {
			return this.action;
		}
	}
	
	

}
