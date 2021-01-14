package enclosure.pi.monitor.service;

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
import enclosure.pi.monitor.common.SharedData;
import enclosure.pi.monitor.db.entity.Config;
import enclosure.pi.monitor.db.sql.ConfigSql;
import enclosure.pi.monitor.service.model.Message;
import enclosure.pi.monitor.service.model.Message.MessageType;

@Path("config")
public class ConfigService {
	
	private static final Logger logger = LogManager.getLogger(ConfigService.class);
	
	@Path("configData")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConfig() {
		
		logger.debug("getConfig. ");


		Message msg = new Message(MessageType.ERROR, "Severe error in getConfig");
		Status status = Status.FORBIDDEN;
		try {
			

			ConfigSql sql = new ConfigSql();
			
			Config cf = sql.loadConfig();


			return Response.ok().entity(cf).build();

		}catch(Exception e) {
			logger.error("Error in getConfig" , e);
			status = Status.BAD_REQUEST;
			msg = new Message(MessageType.ERROR, e.getMessage());
		}

		return Response.status(status).entity(msg).build();	
	}

	@Path("updateConfig")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateConfig(Config configData) {
		
		logger.debug("Update config: " + configData);


		Message msg = new Message(MessageType.ERROR, "Severe error in getConfig");
		Status status = Status.FORBIDDEN;
		try {
			
			
			ConfigSql sql = new ConfigSql();
			sql.updateConfig(configData);
			
			
			msg = new Message(MessageType.SUCCESS, "Config Updated");

			SharedData.getInstance().putSharedObject(Constants.CONFIG, sql.loadConfig());
			
			return Response.ok().entity(msg).build();

		}catch(Exception e) {
			logger.error("Error in updateConfig" , e);
			status = Status.BAD_REQUEST;
			msg = new Message(MessageType.ERROR, e.getMessage());
		}

		return Response.status(status).entity(msg).build();	
	}

}
