package enclosure.pi.monitor.thread;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import enclosure.pi.monitor.printer.PrinterHandler;

public class ThreadManager {

	private static final Logger logger = LogManager.getLogger(ThreadManager.class);

	private static ThreadManager threadManager;


	private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);
	private ScheduledFuture<?> printerShutdown;
	private ScheduledFuture sendSMSThread;
	


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
		
		PrinterHandler.getInstance().getPrintData().setPrinterShutdownInProgress(true);

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
	
	public void sendSmsMessage(SendSMSThread smsThread) {
		logger.debug("sendSmsMessage");
		executorService.submit(smsThread);
	}
	
}
