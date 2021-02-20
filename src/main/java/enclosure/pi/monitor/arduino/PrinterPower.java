package enclosure.pi.monitor.arduino;

import java.io.IOException;

import enclosure.pi.monitor.common.SensorsData;
import enclosure.pi.monitor.common.SharedData;

public class PrinterPower implements Command{
	
	private PowerAction powerAction;
	private ArduinoHandler ah;

	public PrinterPower(PowerAction action) {
		this.powerAction = action;
		this.ah = ArduinoHandler.getInstance();
	}


	public void action() throws IllegalStateException, IOException {
		StringBuilder sb = new StringBuilder();
		sb.append(START_MARKER);
		sb.append(PRINTER_POWER);
		sb.append(this.powerAction.getAction());		
		sb.append(END_MARKER);
		ah.writeToSerial(sb.toString());
		
		SharedData.getInstance().putSensor(SensorsData.POWER_STATUS, getPowerStatus());
	}

	
	public PowerAction getPowerStatus() 
	{
		return powerAction;
	}
	
	
	public enum PowerAction{
		ON(1), OFF(0);
		
		private int action = -1;
		
		private PowerAction (int action) {
			this.action = action;
		}
		
		public int getAction() {
			return this.action;
		}
	}
	
}
