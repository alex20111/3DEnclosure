package enclosure.pi.monitor.arduino;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class TemperatureEnclosure implements Command{
	
	public static final String TEMP_COMMAND = "t";
	
	private ArduinoHandler ah;
	
	public TemperatureEnclosure() {
		ah  = ArduinoHandler.getInstance();
	}
	
	public String getTemperature() throws IllegalStateException, IOException, InterruptedException {
		StringBuilder sb = new StringBuilder();
		sb.append(START_MARKER);
		sb.append(TEMP_COMMAND);
		sb.append(END_MARKER);
		ah.writeToSerial(sb.toString());
		
		String temp = ah.getEncTempQueue().poll(4000, TimeUnit.MILLISECONDS);
		
		return temp;
	}
}
