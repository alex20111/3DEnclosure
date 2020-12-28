package enclosure.pi.monitor.arduino;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Lights implements Command{
	private static final Logger logger = LogManager.getLogger(Lights.class);
	
	private final String LIGHT_COMMAND = "l";


	private LightAction lightAction;
	private ArduinoHandler ah;

	public Lights(LightAction action) {
		this.lightAction = action;
		this.ah = ArduinoHandler.getInstance();
	}


	public void triggerLight() throws IllegalStateException, IOException {
		StringBuilder sb = new StringBuilder();
		sb.append(START_MARKER);
		sb.append(LIGHT_COMMAND);
		sb.append(this.lightAction.getAction());		
		sb.append(END_MARKER);
		ah.writeToSerial(sb.toString());
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
