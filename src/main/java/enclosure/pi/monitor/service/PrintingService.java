package enclosure.pi.monitor.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import enclosure.pi.monitor.common.Constants;
import enclosure.pi.monitor.common.SharedData;
import enclosure.pi.monitor.service.model.Message;
import enclosure.pi.monitor.service.model.Message.MessageType;
import enclosure.pi.monitor.service.model.PrintInfo;

@Path("print")
public class PrintingService {

	private static final Logger logger = LogManager.getLogger(PrintingService.class);
	
	
	private Thread printMsgThrd;
	
	@Path("start")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response startPrinting(PrintInfo printingData) {
		
		logger.debug("Start printing. info: " + printingData + "\nReal time: " + printingData.getEndDateConv());
		SharedData sd = SharedData.getInstance();
		
		sd.putSharedObject(Constants.PRINT_STARTED, printingData.isStarted());
		
		Message msg = new Message(MessageType.SUCCESS,"Printing started");
		
		return Response.ok().entity(msg).build();
	}
	@Path("stop")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response stopPrinting(PrintInfo printingData) {
		
		logger.debug("stop printing. Minutes to completition: " + printingData);
		SharedData sd = SharedData.getInstance();
		
		sd.putSharedObject(Constants.PRINT_STARTED, false);
		
		if (printMsgThrd != null) {
			printMsgThrd.interrupt();
			printMsgThrd = null;
		}
		
		Message msg = new Message(MessageType.SUCCESS,"Printing stopped");
		
		return Response.ok().entity(msg).build();
	}
	

}
 
