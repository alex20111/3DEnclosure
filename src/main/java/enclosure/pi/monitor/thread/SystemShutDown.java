package enclosure.pi.monitor.thread;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import enclosure.pi.monitor.arduino.ExtractorFan;
import enclosure.pi.monitor.arduino.Lights;
import enclosure.pi.monitor.arduino.PrinterPower;
import enclosure.pi.monitor.arduino.ExtractorFan.ExtractorFanCmd;
import enclosure.pi.monitor.arduino.Lights.LightAction;
import enclosure.pi.monitor.arduino.PrinterPower.PowerAction;
import enclosure.pi.monitor.service.GeneralService;
import home.misc.Exec;

public class SystemShutDown implements Runnable{

	private static final Logger logger = LogManager.getLogger(SystemShutDown.class);

	@Override
	public void run() {
		logger.info("Shutting down server");

		//turn off printer
		PrinterPower p = new PrinterPower(PowerAction.OFF);
		try {
			p.action();


			Lights l = new Lights(LightAction.OFF);
			l.triggerLight();

			ExtractorFan e = new ExtractorFan(ExtractorFanCmd.SET_SPEED);
			e.setFanSpeed(0);	

		} catch (IllegalStateException | IOException e1) {
			logger.error("Error", e1);
		}

		Exec exec = new Exec();
		exec.addCommand("sudo").addCommand("shutdown").addCommand("-h").addCommand("now");

		try {
			exec.run();
		} catch (IOException e) {
			logger.error("Error", e);
		}

	}

}
