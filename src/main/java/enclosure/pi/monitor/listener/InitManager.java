package enclosure.pi.monitor.listener;


import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import enclosure.pi.monitor.arduino.ArduinoHandler;
import enclosure.pi.monitor.arduino.Lights.LightAction;
import enclosure.pi.monitor.common.SensorsData;
import enclosure.pi.monitor.common.SharedData;
import enclosure.pi.monitor.thread.MonitorThread;


public class InitManager implements ServletContextListener    {

	private static final Logger logger = LogManager.getLogger(InitManager.class);


	/* Application Startup Event */
	public void contextInitialized(ServletContextEvent ce) 
	{
		logger.debug("Context called at init");

		logger.info("Starting thread manager");

		boolean prod = true;
		File localTest = new File("C:\\dev\\jetty\\devTest.txt");

		if (localTest.exists()) {
			prod = false;
		}

		try {

			if (prod) {
				ArduinoHandler ah = ArduinoHandler.getInstance();
				//
				ah.openSerialConnection();

				new Thread(new MonitorThread(2000)).start();
			}else {
				SharedData.getInstance().setRunningInProd(false);
				logger.info("!!!!!!!! in testing mode !!!!!!!!!!");
			}
		}catch(Exception ex) {
			logger.error("error in contextInitialized", ex);		
		}

		logger.debug("contextInitialized end");
	}


	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub

	}

}
