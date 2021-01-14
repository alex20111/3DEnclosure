package enclosure.pi.monitor.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Path("print")
public class PrintingService {

	private static final Logger logger = LogManager.getLogger(PrintingService.class);
	
	
	@Path("start")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response startPrinting(String printingData) {
		
		logger.debug("Start printing. Minutes to completition: " + printingData);
		
		
		return Response.ok().build();
	}
	
	
}
