package enclosure.pi.monitor.service;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

import enclosure.pi.monitor.arduino.ExtractorFan;
import enclosure.pi.monitor.arduino.ExtractorFan.ExtractorFanCmd;
import enclosure.pi.monitor.common.SensorsData;
import enclosure.pi.monitor.common.SharedData;
import enclosure.pi.monitor.db.entity.Config;
import enclosure.pi.monitor.db.sql.ConfigSql;
import enclosure.pi.monitor.service.model.ExtrFanParam;
import enclosure.pi.monitor.service.model.Message;
import enclosure.pi.monitor.service.model.Message.MessageType;

@Path("fancontrol")
public class FanControlService {
	private static final Logger logger = LogManager.getLogger(FanControlService.class);

	private static final Map<Integer, Integer> speedMap = createMap();


	@Path("extrSpeed") //set extractor fan speed
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response fanControl(String data) {
		logger.debug("fanControl: " + data);		

		Message msg = new Message(MessageType.ERROR, "Severe error in fanControl");
		Status status = Status.FORBIDDEN;

		try {
			String speedString = data.substring(9, data.length() -1);

			int fanSpeed = Integer.parseInt(speedString);

			//we get fan speed by % so we need to convert it to 0 - 255.
			//0 is full speed, 255 is off.

			if (fanSpeed > 100) {
				fanSpeed = 100;
			}else if(fanSpeed < 0) {
				fanSpeed = 0;
			}

			int speed = speedMap.get(fanSpeed);
			logger.debug("new Speed: " + speed);

			ExtractorFan exFan = new ExtractorFan(ExtractorFanCmd.SET_SPEED);
			exFan.setFanSpeed(speed, fanSpeed);		

			msg  = new Message(MessageType.SUCCESS, speedString);

			return Response.ok().entity(msg).build();	

		} catch (Exception e ) {
			logger.error("error in fanControl", e);
			msg = new Message(MessageType.ERROR, e.getMessage());
			status = Status.INTERNAL_SERVER_ERROR;
		}

		return Response.status(status).entity(msg).build();	
	}

	@Path("extrRPM") // get extractor fan RPM
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response getExtractorHoodRPM() {

		logger.debug("Getting RPM start");

		Message msg = new Message(MessageType.ERROR, "Severe error in getExtractorHoodRPM");
		Status status = Status.FORBIDDEN;

		int rpmVal = -1;
		try {
			ExtractorFan exFan = new ExtractorFan(ExtractorFanCmd.GET_RPM);
			rpmVal = exFan.getRpm();

			return Response.ok().entity(rpmVal).build();

		} catch (Exception e) {
			logger.error("error in getExtractorHoodRPM", e);
			msg = new Message(MessageType.ERROR, e.getMessage());
			status = Status.INTERNAL_SERVER_ERROR;
		}

		logger.debug("Getting RPM: " + rpmVal);

		return Response.status(status).entity(msg).build();	
	}
	@Path("extrFanParam") // get extractor fan speed ( by percent )
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExtrFanParam() {
		logger.debug("Getting getExtrFanParam ");

		Message msg = new Message(MessageType.ERROR, "Severe error in getExtrFanParam");
		Status status = Status.FORBIDDEN;

		ConfigSql sql = new ConfigSql();
		Config cfg;
		try {
			cfg = sql.loadConfig();
			ExtrFanParam param = new ExtrFanParam(SharedData.getInstance().getSensorAsInt(SensorsData.EXTR_SPEED), cfg.isExtractorAuto() );

			return Response.ok().entity(param).build();
		} catch (ClassNotFoundException | SQLException e) {
			msg = new Message(MessageType.ERROR, e.getMessage());
			status = Status.INTERNAL_SERVER_ERROR;
		}		

		return Response.status(status).entity(msg).build();	
	}
	@Path("updateExtrFanAuto") // get extractor fan speed ( by percent )
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response extrFanUpdate(Config config) {
		logger.debug("Getting extrFanUpdate:  " + config);

		Message msg = new Message(MessageType.ERROR, "Severe error in extrFanUpdate");
		Status status = Status.FORBIDDEN;

		try {
			ConfigSql sql = new ConfigSql();
			Config cfgFromDb = sql.loadConfig();

			cfgFromDb.setExtractorAuto(config.isExtractorAuto());

			sql.updateConfig(cfgFromDb);
			msg = new Message(MessageType.SUCCESS, "Auto updated");
			return Response.ok().entity(msg).build();
		} catch (ClassNotFoundException | SQLException e) {
			msg = new Message(MessageType.ERROR, e.getMessage());
			status = Status.INTERNAL_SERVER_ERROR;
		}		

		return Response.status(status).entity(msg).build();	
	}

	private static Map<Integer, Integer> createMap() {
		Map<Integer, Integer> result = new HashMap<>();
		result.put(0, 255);
		result.put(10, 229);
		result.put(20, 204);
		result.put(30, 178);
		result.put(40, 153);
		result.put(50, 127);
		result.put(60, 102);
		result.put(70, 76);
		result.put(80, 51);
		result.put(90, 25);
		result.put(100, 0);
		return Collections.unmodifiableMap(result);

	}

}
