package enclosure.pi.monitor.service;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import enclosure.pi.monitor.arduino.PrinterPower;
import enclosure.pi.monitor.arduino.PrinterPower.PowerAction;
import enclosure.pi.monitor.common.Constants;
import enclosure.pi.monitor.printer.PrinterHandler;
import enclosure.pi.monitor.service.model.FileList;
import enclosure.pi.monitor.service.model.Message;
import enclosure.pi.monitor.service.model.Message.MessageType;
import enclosure.pi.monitor.service.model.PrintServiceData;
import enclosure.pi.monitor.thread.ThreadManager;

@Path("print")
public class PrintingService {

	private static final Logger logger = LogManager.getLogger(PrintingService.class);

	/**
	 * Get the print page the information to display
	 * @return
	 */
	@Path("initScreen")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPrintUiInfo() {
		logger.debug("getPrintUiInfo ");		

		try {
			PrinterHandler ph = PrinterHandler.getInstance();		

			PrintServiceData psd = ph.getPrintData();

			List<FileList> fileList = Stream.of(new File(Constants.GCODE_DIR).listFiles())
					.filter(file -> !file.isDirectory())
					.map(file -> new FileList(file.getName(), file.length(), true, false))
					.collect(Collectors.toList());

			if (!psd.isPrinting() && psd.isPrinterConnected()) {
				//get files from SD card.
				List<String> sDfileName = ph.getSdCardFileList();
				fileList.addAll(sDfileName.stream().map(sdFile -> new FileList(sdFile, 0, false, true)).collect(Collectors.toList()));		
			}
			psd.setListFiles(fileList);
			logger.debug("Init printer UI info: " + psd);
			return Response.ok().entity(psd).build();

		}catch (Exception ex) {
			logger.error("Erro init printier info" , ex);
		}

		return Response.ok().build(); //TODO correct
	}

	/**
	 * Start a print
	 * @param printData
	 * @return
	 */
	@Path("start")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response startPrinting(PrintServiceData printData) {

		logger.debug("Start printing. info: " + printData );

		Message msg = new Message(MessageType.ERROR,"Printing error");
		Status status = Status.FORBIDDEN;

		if (printData.getPrintFile() != null) {
			status = Status.OK;
			PrinterHandler ph = PrinterHandler.getInstance();
			//check if printer is connected 1st.. if not.. tur
			PrintServiceData psd = ph.getPrintData();
			psd.setAutoPrinterShutdown(printData.isAutoPrinterShutdown());

			try {
				if (!psd.isPrinterConnected()) {
					msg = powerOnPrinter(psd, msg);				
				}
			}catch(IOException | IllegalStateException | InterruptedException e) {
				logger.error("Error while trying to turn on printer", e);
			}

			if(psd.isPrinterConnected()) {

				if (!psd.isPrinting() ) {
					logger.debug("Starting printing file: " + printData.getPrintFile());

					try {
						FileList fileToPrint = printData.getPrintFile();
						if (fileToPrint.isFileFromSd()) {
							ph.startPrintFromSD(fileToPrint);

						}else if (fileToPrint.isFileFromPi()) {

							ph.sendGcodeFileToPrinter(printData.getPrintFile());
						}
						msg = new Message(MessageType.SUCCESS, "Printing started");
					} catch (FileNotFoundException e) {
						msg = new Message(MessageType.WARN, "File " + printData.getPrintFile() + " not found");
					}catch (IllegalAccessError ae) {
						msg = new Message(MessageType.WARN, "Printer not connected");
					}

				}else {				
					msg = new Message(MessageType.WARN, "Printer is already printing");
				}
			}else {
				msg = new Message(MessageType.WARN, "Printer Not connected");
			}
		}		
		return Response.status(status).entity(msg).build();
	}
	/**
	 * Stop a print in process.
	 * 
	 * @param printingData
	 * @return
	 */
	@Path("stop")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response stopPrinting(PrintServiceData printingData) {

		logger.debug("stop printing. " + printingData);
		Message msg = null;
		try {

			PrinterHandler ph = PrinterHandler.getInstance();
			if (ph.isPrinting()) {
				ph.stopPrinting();
				msg = new Message(MessageType.SUCCESS, "Print stopped, Stopping fan function in 5 min");
			}else {
				msg = new Message(MessageType.SUCCESS, "No print in progress");
			}


		}catch (Exception e) {
			logger.error("error stopping thread" , e);
			msg = new Message(MessageType.WARN,"Error stopping printing. " + e.getMessage());
		}

		return Response.ok().entity(msg).build();
	}

	/**
	 * Turn the printer on / off
	 * @param data
	 * @return
	 */
	@Path("printerOnOff")
	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response printerPower(String data) {
		logger.debug("printer on/off: " + data);

		Message msg = new Message(MessageType.ERROR, "Severe error in printerPower");
		Status status = Status.FORBIDDEN;

		try {

			PrinterHandler ph = PrinterHandler.getInstance();
			PrintServiceData psd = ph.getPrintData();

			if (psd.isPrinting()) {
				//turn off printing 1st
				ph.stopPrinting();
			}

			if ("turnOn".equalsIgnoreCase(data)) {
				if (psd.isPrinterConnected()) {
					msg = new Message(MessageType.SUCCESS, "on");
				}else {

					msg = powerOnPrinter(psd, msg);
				}

			}else {

				if (psd.getNozzleTemp() > 120) {
					msg = new Message(MessageType.SUCCESS, "offwithdelay");
					ThreadManager.getInstance().shutdownPrinter(5, TimeUnit.MINUTES);
				}else {
					PrinterPower printPower = new PrinterPower(PowerAction.OFF);
					printPower.action();
					msg = new Message(MessageType.SUCCESS, "off");

					//wait until printer is disconnected
					int cnt = 0;
					while(psd.isPrinterConnected() && cnt < 30) {
						Thread.sleep(500);
						cnt ++;
					}
				}
			}

			return Response.ok().entity(msg).build();

		}catch(Exception e) {
			logger.error("Error in printerPower" , e);
			status = Status.BAD_REQUEST;
			msg = new Message(MessageType.ERROR, e.getMessage());
		}

		return Response.status(status).entity(msg).build();	
	}
	@Path("pausePrinter")
	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response pausePrint(String data) {		
		logger.debug("Pause/resume printer " + data);

		Message msg = new Message(MessageType.ERROR, "Severe error in pausePrint");
		Status status = Status.FORBIDDEN;

		PrinterHandler ph = PrinterHandler.getInstance();

		try {
			if ("pause".equalsIgnoreCase(data)) {

				ph.pausePrint();

			}else if("resume".equalsIgnoreCase(data)) {
				ph.resumePrint();
			}
			status = Status.OK;
			msg = new Message(MessageType.SUCCESS, "Printer: " + data);
		}catch(Exception ex) {
			logger.error("Error in pause print" , ex);
			msg = new Message(MessageType.ERROR, "Exception in pausePrint");
		}

		return Response.status(status).entity(msg).build();
	}
	/**
	 * Stop the automatic shutdown of the printer 
	 * @return
	 */
	@Path("stopShutdown")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response stopPrinterShutdown() {
		logger.debug("stopPrinterShutdown ");	

		Message msg = new Message(MessageType.ERROR, "Severe error in printerPower");
		Status status = Status.FORBIDDEN;

		try {

			ThreadManager.getInstance().overridePrinterShutdown();

			msg = new Message(MessageType.SUCCESS, "Printer auto shutdown stopped");

			PrintServiceData pds =  PrinterHandler.getInstance().getPrintData();
			pds.setAutoPrinterShutdown(false);
			pds.setPrinterShutdownInProgress(false);

			return Response.ok().entity(msg).build();

		}catch (Exception ex) {
			logger.error("Erro init printier info" , ex);
		}

		return Response.status(status).entity(msg).build(); //TODO correct
	}

	/**
	 * Stop the automatic shutdown of the printer 
	 * @return
	 */
	@Path("autoShutdown")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.TEXT_PLAIN)
	public Response autoShutdownPrinter(String autoShutdown) {
		logger.debug("autoShutdownPrinter:  " + autoShutdown);	

		Message msg = new Message(MessageType.ERROR, "Severe error in autoShutdownPrinter");
		Status status = Status.FORBIDDEN;

		try {
			//true or false;
			boolean autoShutdownPrinter = Boolean.valueOf(autoShutdown);

			PrintServiceData ps = PrinterHandler.getInstance().getPrintData();
			
			ps.setAutoPrinterShutdown(autoShutdownPrinter);
			
			msg = new Message(MessageType.SUCCESS, "Value set to : " + autoShutdownPrinter);
			
			return Response.ok().entity(msg).build();

		}catch (Exception ex) {
			logger.error("Error autoShutdownPrinter" , ex);
			 msg = new Message(MessageType.ERROR, ex.getMessage());
		}

		return Response.status(status).entity(msg).build(); //TODO correct
	}

	private Message powerOnPrinter(PrintServiceData psd, Message msg) throws IllegalStateException, IOException, InterruptedException {
		PrinterPower printPower = new PrinterPower(PowerAction.ON);
		printPower.action();
		//wait until printer connect
		for (int i = 0 ; i < 10; i ++) {
			//wait for 20 sec to see if printer will connect
			if (psd.isPrinterConnected()) {
				msg = new Message(MessageType.SUCCESS, "on");
				break;
			}
			Thread.sleep(2000);
		}
		if (!psd.isPrinterConnected()) {
			msg = new Message(MessageType.WARN, "Could not connect to printer");
		}

		return msg;
	}
}
