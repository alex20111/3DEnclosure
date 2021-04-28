package enclosure.pi.monitor.thread;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import home.misc.Exec;

public class StreamWebcamThread implements Runnable {

	private static final Logger logger = LogManager.getLogger(StreamWebcamThread.class);
	@Override
	public void run() {
		logger.debug("StreamWebcamThread at start");
		
		
		Exec e  = new Exec();
		e.addCommand("sudo").addCommand("motion");
		
		try {
			e.run();
		} catch (IOException e1) {
			logger.error("Error in StreamWebcamThread", e1);
		}
		
		
		logger.debug("StreamWebcamThread:: END");
	}

}
