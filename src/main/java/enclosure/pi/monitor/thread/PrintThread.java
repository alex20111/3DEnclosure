package enclosure.pi.monitor.thread;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import enclosure.pi.monitor.service.model.PrintInfo;

public class PrintThread implements Runnable {
	private static final Logger logger = LogManager.getLogger(PrintThread.class);


	public PrintThread() {
	}

	@Override
	public void run() {


		logger.debug("Sending FINISH MESSAGE!!!!!!!!!!!!!");
		SendSMSThread sms = new SendSMSThread("Print Finished", "Your print is finished, please verify");

		ThreadManager.getInstance().sendSmsMessage(sms);


	}

}
