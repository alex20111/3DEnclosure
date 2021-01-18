package enclosure.pi.monitor.listener;


import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import enclosure.pi.monitor.arduino.ArduinoHandler;
import enclosure.pi.monitor.arduino.Lights;
import enclosure.pi.monitor.arduino.Lights.LightAction;
import enclosure.pi.monitor.common.Constants;
import enclosure.pi.monitor.common.SensorsData;
import enclosure.pi.monitor.common.SharedData;
import enclosure.pi.monitor.db.entity.Config;
import enclosure.pi.monitor.db.sql.ConfigSql;
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

		SharedData sd = SharedData.getInstance();
		if (localTest.exists()) {
			prod = false;
			sd.setRunningInProd(false);
			logger.info("!!!!!!!! in testing mode !!!!!!!!!!");
		}
		
		

		try {
			ConfigSql sql = new ConfigSql();
			
			
			sql.createConfigTable();

			Config cfg = sql.loadConfig();
			sd.putSharedObject(Constants.CONFIG, cfg);

			if (prod) {
				ArduinoHandler ah = ArduinoHandler.getInstance();
				//
				ah.openSerialConnection();

				//wait until arduino ready
				int cnt = 0;
				while(!ah.isArduinoReady() && cnt < 20) {
					cnt++;
					Thread.sleep(1000);
				}
				
				new Thread(new MonitorThread(3000)).start();

			}else {
				
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
