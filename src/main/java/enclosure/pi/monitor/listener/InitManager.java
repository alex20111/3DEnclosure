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

		if (localTest.exists()) {
			prod = false;
		}

		try {
			ConfigSql sql = new ConfigSql();
			SharedData sd = SharedData.getInstance();
			sql.createConfigTable();

			Config cfg = sql.loadConfig();

			if (prod) {
				ArduinoHandler ah = ArduinoHandler.getInstance();
				//
				ah.openSerialConnection();

				new Thread(new MonitorThread(2000)).start();

				//wait until arduino ready
				int cnt = 0;
				while(!ah.isArduinoReady() && cnt < 20) {
					cnt++;
					Thread.sleep(1000);
				}
				if (cfg.isLightsOn()) {
					logger.info("Auto turning lights");
					Lights l = new Lights(LightAction.ON);
					l.triggerLight();
				}
			}else {
				sd.setRunningInProd(false);
				logger.info("!!!!!!!! in testing mode !!!!!!!!!!");
			}


			sd.putSharedObject(Constants.CONFIG, cfg);


		}catch(Exception ex) {
			logger.error("error in contextInitialized", ex);		
		}

		logger.debug("contextInitialized end");
	}


	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub

	}

}
