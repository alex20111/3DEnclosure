package enclosure.pi.monitor.service;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import enclosure.pi.monitor.arduino.TemperatureEnclosure;
import enclosure.pi.monitor.common.SensorsData;
import enclosure.pi.monitor.common.SharedData;
import enclosure.pi.monitor.service.model.Message;
import enclosure.pi.monitor.service.model.Message.MessageType;
@Path("temperature")
public class TemperatureService {

	private static final Logger logger = LogManager.getLogger(TemperatureService.class);


	@Path("enclosureTemp")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response enclosureTemperature() {

		logger.debug("enclosureTemperature");

		Message msg = new Message(MessageType.ERROR, "Severe error in enclosureTemperature");
		Status status = Status.FORBIDDEN;

		String temperature = "-99";
		try {
			TemperatureEnclosure tmp = new TemperatureEnclosure();
			temperature = tmp.getTemperature();
			
			SharedData.getInstance().putSensor(SensorsData.ENC_TEMP, temperature);

			return Response.ok().entity(temperature).build();

		} catch (Exception e) {
			logger.error("error in enclosureTemperature", e);
			msg = new Message(MessageType.ERROR, e.getMessage());
			status = Status.INTERNAL_SERVER_ERROR;
		}

		logger.debug("enclosureTemperature: " + temperature);

		return Response.status(status).entity(msg).build();	
	}

	@Path("electronicTemp")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response elecTronicsTemperature() {

		return null;	
	}
}
