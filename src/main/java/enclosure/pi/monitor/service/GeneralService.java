package enclosure.pi.monitor.service;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.exec.ExecuteException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import enclosure.pi.monitor.arduino.Lights.LightAction;
import enclosure.pi.monitor.common.SensorsData;
import enclosure.pi.monitor.common.SharedData;
import enclosure.pi.monitor.db.sql.ConfigSql;
import enclosure.pi.monitor.service.model.DashBoard;
import enclosure.pi.monitor.service.model.Message;
import enclosure.pi.monitor.service.model.Message.MessageType;
import home.misc.Exec;

@Path("general")
public class GeneralService {

	private static final Logger logger = LogManager.getLogger(GeneralService.class);
	private boolean isServerShuttingDown = false;


	@Path("dashboard")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response dashBoard() {
		
		logger.debug("dashBoard. ");


		Message msg = new Message(MessageType.ERROR, "Severe error in dashBoard");
		Status status = Status.FORBIDDEN;
		try {
			

			final SharedData sd = SharedData.getInstance();	
			final ConfigSql sql = new ConfigSql();
			
			
			DashBoard db = new DashBoard();

			db.setAirQualityCo2(sd.getSensorAsString(SensorsData.AIR_CO2));
			db.setAirQualityVoc(sd.getSensorAsString(SensorsData.AIR_VOC));
			db.setExtracFanRPM(sd.getSensorAsInt(SensorsData.EXTR_RPM));
			db.setExtracFanSpeed(sd.getSensorAsInt(SensorsData.EXTR_SPEED));
			
			LightAction la = sd.getSensor(SensorsData.LIGHT_STATUS) != null ? (LightAction) sd.getSensor(SensorsData.LIGHT_STATUS) : LightAction.OFF;
			db.setLightOn(la == LightAction.ON ?  true: false);
			db.setTemperature(sd.getSensorAsString(SensorsData.ENC_TEMP));
			db.setExtrFanOnAuto(sql.loadConfig().isExtractorAuto());


			return Response.ok().entity(db).build();

		}catch(Exception e) {
			logger.error("Error in dashBoard" , e);
			status = Status.BAD_REQUEST;
			msg = new Message(MessageType.ERROR, e.getMessage());
		}

		return Response.status(status).entity(msg).build();	
	}



	@Path("shutdown")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response shutDown() {

		logger.debug("Shutdown. isServerShuttingDown: " + isServerShuttingDown);


		Message msg = new Message(MessageType.ERROR, "Severe error in shutDown");
		Status status = Status.FORBIDDEN;

		try {
			if (isServerShuttingDown) {
				msg = new Message(MessageType.WARN, "Server already shutting down");				
			}else {
				isServerShuttingDown = true;
				msg = new Message(MessageType.SUCCESS, "Shutting down in 20 sec");

				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							logger.info("in thread, starting shutdown sequence...");
							Thread.sleep(20000);
							Exec exec = new Exec();
							exec.addCommand("sudo").addCommand("shutdown").addCommand("-h").addCommand("now");

							exec.run();


						} catch (InterruptedException e) {

						} catch (ExecuteException e) {
							logger.error("Execute error", e);
						} catch (IOException e) {
							logger.error("IOException error", e);
						}

					}

				}).start();
			}		

			return Response.ok().entity(msg).build();	

		}catch(Exception e) {
			logger.error("Error in shutDown" , e);
			status = Status.BAD_REQUEST;
			msg = new Message(MessageType.ERROR, e.getMessage());
		}

		return Response.status(status).entity(msg).build();	


	}
}
