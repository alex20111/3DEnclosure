package enclosure.pi.monitor.listener;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class InitManager implements ServletContextListener    {

	private static final Logger logger = LogManager.getLogger(InitManager.class);
	

	/* Application Startup Event */
	public void contextInitialized(ServletContextEvent ce) 
	{
		logger.debug("Context called at init");

		logger.info("Starting thread manager");

//		try {
//			ThreadManager tm = ThreadManager.getInstance();
//
//			if (runningOnPi) {
//				long sampleRate = 1000 * 60 * 5; // 5 min
//				logger.info("Starting temperature with sample rate: " + sampleRate + " in millis. In min: " + ( (sampleRate /1000) / 60) );
//				tm.startTemperature(sampleRate);
//				
//				SerialHandler sh = SerialHandler.getInstance();
//				sh.startCeiscoSerial();
//				sh.startTeensySerial();
//				
//			}else {
//				logger.info("RUNNING IN DEV MODE");
//			}
//		}catch(Exception ex) {
//			ex.printStackTrace();			
//		}

		logger.debug("contextInitialized end");
	}


	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub

	}

}
