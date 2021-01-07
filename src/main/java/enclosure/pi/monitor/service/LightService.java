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

import enclosure.pi.monitor.arduino.Lights;
import enclosure.pi.monitor.arduino.Lights.LightAction;
import enclosure.pi.monitor.common.SensorsData;
import enclosure.pi.monitor.service.model.Message;
import enclosure.pi.monitor.service.model.Message.MessageType;

@Path("light")
public class LightService {
	
	private static final Logger logger = LogManager.getLogger(LightService.class);
	
	
	@Path("lightControl")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response lightControl(String data) {
		
		logger.debug("Light control: " + data);
		
		Message msg = new Message(MessageType.ERROR, "Severe error in lightControl");
		Status status = Status.FORBIDDEN;

		try {
			
			String value = data.substring(10, data.length() -1 );
			
			boolean val = Boolean.valueOf(value);
//			logger.debug("Value: " + val);
			
			Lights light = null;
			if (val) {
				light = new Lights(LightAction.ON);
				
			}else {
				light = new Lights(LightAction.OFF);
			}
			
			light.triggerLight();
			//setLightStatus(light.getLightStatus()); //sending to common status.
			
			msg = new Message(MessageType.SUCCESS, value);
			
			return Response.ok().entity(msg).build();
			
		}catch(Exception e) {
			logger.error("Error in lightControl" , e);
			status = Status.BAD_REQUEST;
			msg = new Message(MessageType.ERROR, e.getMessage());
		}
		
		return Response.status(status).entity(msg).build();	
	}

}
