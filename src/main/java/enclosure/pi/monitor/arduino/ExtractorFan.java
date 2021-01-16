package enclosure.pi.monitor.arduino;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import enclosure.pi.monitor.common.SensorsData;
import enclosure.pi.monitor.common.SharedData;

public class ExtractorFan implements Command {

	private static final Logger logger = LogManager.getLogger(ExtractorFan.class);	

	private ExtractorFanCmd cmd;
	private ArduinoHandler ah;

	private static final Map<Integer, Integer> speedMap = createMap();

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

			SharedData.getInstance().putSensor(SensorsData.EXTR_RPM, rpmVal); //saving to use in recall.

		} catch (Exception e) {
			logger.error("Timeout while waiting in the queue" , e);
		} 

		return rpmVal;
	}

	public void setFanSpeed( int speedPercent) throws IllegalStateException, IOException {
		int speed = speedMap.get(speedPercent);

		StringBuilder sb = new StringBuilder();
		sb.append(START_MARKER);
		sb.append(cmd.getCmdStr());
		sb.append(speed);
		sb.append(END_MARKER);
		ah.writeToSerial(sb.toString());

		SharedData.getInstance().putSensor(SensorsData.EXTR_SPEED, speedPercent); //saving to use in recall.
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

	/**
	 * Increase the speed of the Fan.
	 * @return - true when the fan is at max speed.
	 */
	public boolean increaseSpeed() {

		boolean maxSpeed = false;
		try {
			int speed = SharedData.getInstance().getSensorAsInt(SensorsData.EXTR_SPEED);

			if (speed < 100) {
				speed += speed + 10;
				setFanSpeed(speed);

				if (speed == 100){
					maxSpeed = true;

				}
				SharedData.getInstance().putSensor(SensorsData.EXTR_SPEED, speed);
			}else if (speed == 100) {
				maxSpeed = true;
			}
		}catch(Exception ex) {
			logger.error("Error while trying to increase fans speed", ex);
		}

		return maxSpeed;
	}
	/**
	 * decrease the speed of the Fan.
	 * @return - true when the fan is at max speed.
	 */
	public boolean decreaseSpeed() {
		boolean speedStopped = false;
		try {
			int speed = SharedData.getInstance().getSensorAsInt(SensorsData.EXTR_SPEED);

			if (speed > 0) {
				speed += speed - 10;
				setFanSpeed(speed);

				if (speed == 0){
					speedStopped = true;

				}
				SharedData.getInstance().putSensor(SensorsData.EXTR_SPEED, speed);
			}else if (speed == 0) {
				speedStopped = true;
			}
		}catch(Exception ex) {
			logger.error("Error while trying to increase fans speed", ex);
		}

		return speedStopped;
	}

	private static Map<Integer, Integer> createMap() {
		Map<Integer, Integer> result = new HashMap<>();
		result.put(0, 255);
		result.put(10, 229);
		result.put(20, 204);
		result.put(30, 178);
		result.put(40, 153);
		result.put(50, 127);
		result.put(60, 102);
		result.put(70, 76);
		result.put(80, 51);
		result.put(90, 25);
		result.put(100, 0);
		return Collections.unmodifiableMap(result);

	}

}
