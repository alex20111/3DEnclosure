package enclosure.pi.monitor.thread;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import enclosure.pi.monitor.arduino.PrinterPower;
import enclosure.pi.monitor.arduino.PrinterPower.PowerAction;
import enclosure.pi.monitor.printer.PrinterHandler;

public class PrinterShutdown implements Runnable{

	private static final Logger logger = LogManager.getLogger(PrinterShutdown.class);
	
	@Override
	public void run() {
		logger.info("Shutting down printer");
		
		PrinterPower pp = new PrinterPower(PowerAction.OFF);
		try {
			pp.action();
			
			PrinterHandler.getInstance().getPrintData().setPrinterShutdownInProgress(false);
		} catch (IllegalStateException | IOException e) {
		}
		
		
	}

}
