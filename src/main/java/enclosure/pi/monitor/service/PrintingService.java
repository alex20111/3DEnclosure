package enclosure.pi.monitor.service;



import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
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

import enclosure.pi.monitor.common.Constants;
import enclosure.pi.monitor.printer.PrinterHandler;
import enclosure.pi.monitor.service.model.FileList;
import enclosure.pi.monitor.service.model.Message;
import enclosure.pi.monitor.service.model.Message.MessageType;
import enclosure.pi.monitor.service.model.PrintServiceData;

@Path("print")
public class PrintingService {

	private static final Logger logger = LogManager.getLogger(PrintingService.class);


	@Path("initScreen")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPrintUiInfo() {
		logger.debug("getPrintUiInfo ");		
		
		PrinterHandler ph = PrinterHandler.getInstance();//TODO uncomment for prod
		
		
		PrintServiceData psd = new PrintServiceData();
		
		psd.setPrinting(ph.isPrinting()); //TODO uncomment for prod
		 List<FileList> fileList = Stream.of(new File(Constants.GCODE_DIR).listFiles())
			      .filter(file -> !file.isDirectory())
			      .map(file -> new FileList(file.getName(), file.length()))
			      .collect(Collectors.toList());
		 logger.debug("File list: " + fileList);
		 
		 psd.setListFiles(fileList);
		  
		

		return Response.ok().entity(psd).build();
	}
	
	
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
			if (!ph.isPrinting()) {
				logger.debug("Starting printing file: " + printData.getPrintFile());
				
				try {
					ph.sendGcodeFileToPrinter(printData.getPrintFile());
					msg = new Message(MessageType.SUCCESS, "Printing started");
				} catch (FileNotFoundException e) {
					msg = new Message(MessageType.WARN, "File " + printData.getPrintFile() + " not found");
				}
				
			}else {				
				msg = new Message(MessageType.WARN, "Printer is already printing");
			}
			
		}
		

		return Response.status(status).entity(msg).build();
	}
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
			}
			
			
		}catch (Exception e) {
			logger.error("error stopping thread" , e);
			msg = new Message(MessageType.WARN,"Error stopping printing. " + e.getMessage());
		}



		return Response.ok().entity(msg).build();
	}


}

