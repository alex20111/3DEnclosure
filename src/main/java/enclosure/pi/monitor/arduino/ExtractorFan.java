package enclosure.pi.monitor.arduino;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExtractorFan implements Command {

	private static final Logger logger = LogManager.getLogger(ExtractorFan.class);


	private ExtractorFanCmd cmd;
	private ArduinoHandler ah;

	public ExtractorFan(ExtractorFanCmd command) {
		this.cmd = command;
		this.ah = ArduinoHandler.getInstance();
	}


	public int getRpm() throws IllegalStateException, IOException {
		int rpmVal = -1;

		StringBuilder sb = new StringBuilder();
		sb.append(START_MARKER);
		sb.append(cmd.getCmdStr());
		sb.append(END_MARKER);
		try {
			ah.writeToSerial(sb.toString());
			rpmVal = ah.extFanRpmQueue().poll(4000, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			logger.error("Timeout while waiting in the queue" , e);
		} 

		return rpmVal;
	}

	public void setFanSpeed(int speed) throws IllegalStateException, IOException {
		StringBuilder sb = new StringBuilder();
		sb.append(START_MARKER);
		sb.append(cmd.getCmdStr());
		sb.append(speed);
		sb.append(END_MARKER);
		ah.writeToSerial(sb.toString());
	}

	public enum ExtractorFanCmd{
		GET_RPM(GET_RPM_CMD), SET_SPEED(SET_SPEED_CMD);

		private String cmdStr = "";
		private ExtractorFanCmd(String cmdStr) {
			this.cmdStr = cmdStr;
		}

		public String getCmdStr() {
			return this.cmdStr;
		}
	}

}
