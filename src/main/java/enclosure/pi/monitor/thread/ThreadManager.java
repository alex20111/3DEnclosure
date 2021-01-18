package enclosure.pi.monitor.thread;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ThreadManager {

	private static final Logger logger = LogManager.getLogger(ThreadManager.class);

	private static ThreadManager threadManager;


	private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);
	private ScheduledFuture printThreadResult;
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
	public void startPrint(LocalDateTime endDate) {

		LocalDateTime now = LocalDateTime.now();

		long delay =  ChronoUnit.MILLIS.between(now, endDate);

		logger.debug("startPrint Difference in millis: " + delay);

		printThreadResult = executorService.schedule(new PrintThread(), delay, TimeUnit.MILLISECONDS);
	}
	/**
	 * 
	 */
	public void stopPrint() {
		logger.debug("stopPrint. Is printThreadResult not null? " + ( printThreadResult != null ? "Is not null" : "is null"));

		if (printThreadResult != null) {
			printThreadResult.cancel(true);
		}

	}
	
	public void sendSmsMessage(SendSMSThread smsThread) {
		logger.debug("sendSmsMessage");
		executorService.submit(smsThread);
	}
	
}
