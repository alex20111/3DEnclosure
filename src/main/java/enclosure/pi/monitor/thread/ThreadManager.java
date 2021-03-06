package enclosure.pi.monitor.thread;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.exec.ExecuteException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import enclosure.pi.monitor.printer.PrinterHandler;
import home.misc.Exec;

public class ThreadManager {

	private static final Logger logger = LogManager.getLogger(ThreadManager.class);

	private static ThreadManager threadManager;


	private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(6);
	private ScheduledFuture<?> printerShutdown;
	private ScheduledFuture<?> serverShutdown;
	private ScheduledFuture<?> streamWebcam;
	@SuppressWarnings("unused")
	private ScheduledFuture<?> sendSMSThread;

	private ScheduledFuture<?> serialListener;



	private ThreadManager() {}


	public static ThreadManager getInstance() {
		if (threadManager == null) {
			synchronized (ThreadManager.class) {
				if(threadManager == null) {
					logger.info( "SharedData initialized");
					threadManager = new ThreadManager();
				}
			}
		}
		return threadManager;
	}	

	/**
	 * 
	 * @param endDate
	 */
	public void shutdownPrinter(int time, TimeUnit unit) {		

		long delay = unit.toMillis(time);

		logger.debug("ShutDown printer in: " + time + " Unit: " + unit + "  delay millis: " + delay);

		//todo cancel previous one if any
		if (printerShutdown != null) {
			overridePrinterShutdown();
		}

		PrinterHandler ph = PrinterHandler.getInstance();
		ph.getPrintData().setPrinterShutdownInProgress(true);
		//send command to turn off bed and nozzle 
		try {
			ph.sendCommand("M104 S0", 0);
			Thread.sleep(1000);
			ph.sendCommand("M140 S0", 0);
		} catch (IOException | InterruptedException e) {
			logger.error("error in shutdownPrinter setting hot/bed temperature",e);
		}

		printerShutdown = executorService.schedule(new PrinterShutdown(), delay, TimeUnit.MILLISECONDS);
	}
	/**
	 * 
	 */
	public void overridePrinterShutdown() {
		logger.debug("overridePrinterShutdown. Is printerShutdown not null? " + ( printerShutdown != null ? "Is not null" : "is null"));

		if (printerShutdown != null) {
			printerShutdown.cancel(true);
			int cnt = 0;
			while(!printerShutdown.isDone() && cnt < 20) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				cnt ++;
			}
			if (cnt == 20) {
				logger.info("Could not terminate thread - overridePrinterShutdown" );
			}
		}

	}

	public void startPrinterSerialListener(BlockingQueue<String> queue, Path fileName) {
		
		stopPrinterSerialListener();
		
		serialListener = executorService.schedule(new PrinterSerialConsoleThread(queue, fileName), 0, TimeUnit.MILLISECONDS);
	}
	public void stopPrinterSerialListener() {
		logger.debug("stopPrinterSerialListener. Is printerShutdown not null? " + ( serialListener != null ? "Is not null" : "is null"));

		if (serialListener != null) {
			serialListener.cancel(true);
			int cnt = 0;
			while(!serialListener.isDone() && cnt < 20) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				cnt ++;
			}
			if (cnt == 20) {
				logger.info("Could not terminate thread - serialListener" );
			}else {

				serialListener = null;
			}
		}
	}

	public void sendSmsMessage(SendSMSThread smsThread) {
		logger.debug("sendSmsMessage");
		executorService.submit(smsThread);
	}
	
	/**
	 * 
	 * Shut down the server
	 */
	public void shutdownServer() {		

		//todo cancel previous one if any
		if (serverShutdown != null) {
			overrideSystemShutdown();
		}

		serverShutdown = executorService.schedule(new SystemShutDown(), 20, TimeUnit.SECONDS);
	}
	/**
	 * 
	 */
	public void overrideSystemShutdown() {
		logger.debug("overrideSystemShutdown. Is serverShutdown not null? " + ( serverShutdown != null ? "Is not null" : "is null"));

		if (serverShutdown != null) {
			serverShutdown.cancel(true);
			int cnt = 0;
			while(!serverShutdown.isDone() && cnt < 20) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				cnt ++;
			}
			if (cnt == 20) {
				logger.info("Could not terminate thread - serverShutdown" );
			}
		}

	}
	/**
	 * 
	 * Shut down the server
	 * @throws IOException 
	 * @throws ExecuteException 
	 */
	public void streamWebcamThread() throws ExecuteException, IOException {		

		//todo cancel previous one if any
		if (streamWebcam != null) {
			stopStreamWebcamThread();
		}

		streamWebcam = executorService.schedule(new StreamWebcamThread(), 0, TimeUnit.SECONDS);
	}
	/**
	 * @throws IOException 
	 * @throws ExecuteException 
	 * 
	 */
	public void stopStreamWebcamThread() throws ExecuteException, IOException {
		logger.debug("stopStreamWebcamThread. Is streamWebcam not null? " + ( streamWebcam != null ? "Is not null" : "is null"));

		if (streamWebcam != null) {
			
			Exec e = new Exec();
			e.addCommand("sudo").addCommand("pkill").addCommand("motion");
			int run = e.run();
			
			logger.debug("stopStreamWebcamThread pkill command executed with: " + run );
			
			streamWebcam.cancel(true);
			int cnt = 0;
			while(!streamWebcam.isDone() && cnt < 20) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				cnt ++;
			}
			if (cnt == 20) {
				logger.info("Could not terminate thread - streamWebcam" );
			}else {
				streamWebcam = null;
			}
		}

	}
	public boolean isServerInProcessOfShutDown() {
		return this.serverShutdown != null && !this.serverShutdown.isDone();
	}

}
