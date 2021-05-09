package enclosure.pi.monitor.service;



import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import enclosure.pi.monitor.service.model.Message;
import enclosure.pi.monitor.service.model.Message.MessageType;
import enclosure.pi.monitor.thread.ThreadManager;
import home.misc.Exec;

@Path("general")
public class GeneralService {

	private static final Logger logger = LogManager.getLogger(GeneralService.class);



	@Path("shutdown")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.TEXT_PLAIN)
	public Response shutDown(String command) {

		logger.debug("Shutdown. command String: " + command );

		//Shutting down = "ShuttingDown"
		//Override shutting down = "CancelShutDown"


		Message msg = new Message(MessageType.ERROR, "Severe error in shutDown");
		Status status = Status.FORBIDDEN;


		try {

			ThreadManager tm = ThreadManager.getInstance();

			logger.debug("Is server shutting down? : " + tm.isServerInProcessOfShutDown());

			if ("ShuttingDown".equals(command) && !tm.isServerInProcessOfShutDown()) {
				msg = new Message(MessageType.SUCCESS, "Shutting down in 20 sec");
				tm.shutdownServer();
			}else if ("CancelShutDown".equals(command) && tm.isServerInProcessOfShutDown()) { //return 
				tm.overrideSystemShutdown();

				msg = new Message(MessageType.WARN, "Interrupted");
			}


			return Response.ok().entity(msg).build();	

		}catch(Exception e) {
			logger.error("Error in shutDown" , e);
			status = Status.BAD_REQUEST;
			msg = new Message(MessageType.ERROR, e.getMessage());
		}

		return Response.status(status).entity(msg).build();	


	}
	@Path("stream")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.TEXT_PLAIN)
	public Response stream(String command) {

		logger.debug("stream webcam: " + command );

		//TurnOn = "turnOn webcam"
		//TurnOff = "turnOff webcam"
		//status = "Status of the webcam"


		Message msg = new Message(MessageType.ERROR, "Severe error in stream");
		Status status = Status.FORBIDDEN;


		try {

			ThreadManager tm = ThreadManager.getInstance();

			if ("TurnOn".equals(command)) {
				msg = new Message(MessageType.SUCCESS, "Webcam Started");
				tm.streamWebcamThread();
			}else if ("TurnOff".equals(command) ) { //return 
				tm.stopStreamWebcamThread();

				msg = new Message(MessageType.WARN, "Webcam stopped");
			}else if ("status".equals(command)) {

				Exec e = new Exec();
				e.addCommand("sudo").addCommand("pgrep").addCommand("-x").addCommand("\"motion\"");

				int rtn = e.run();

				logger.debug("Webcam status:  " + rtn + " get output: " + e.getOutput()); 
				try {
					if (rtn == 0 && Integer.parseInt(e.getOutput().trim()) > 0) {
						msg = new Message(MessageType.SUCCESS, "Webcam connected");
					}else {
						msg = new Message(MessageType.WARN, "Webcam cannot be reached");
					}
				}catch(NumberFormatException nfe) {
					msg = new Message(MessageType.ERROR, "Webcam cannot be reached");
				}
			}


			return Response.ok().entity(msg).build();	

		}catch(Exception e) {
			logger.error("Error in stream" , e);
			status = Status.BAD_REQUEST;
			msg = new Message(MessageType.ERROR, e.getMessage());
		}

		return Response.status(status).entity(msg).build();	
	}

}
