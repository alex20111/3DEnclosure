package enclosure.pi.monitor.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.websocket.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jsoniter.JsonIterator;

public class WebSocketHandler {

	private static final Logger logger = LogManager.getLogger(WebSocketHandler.class);

	//Angular will be registered in the user Session..
	//the server (java) will not be registerd , only send messages.

	private List<UserSession> userSessions = new ArrayList<UserSession>();
	private String printData = null;  //Socket message containing the print data.

	public void processMessage(String message, Session session) throws IOException {

		SocketMessage msg = JsonIterator.deserialize(message, SocketMessage.class);

		if (msg.getAction() == WsAction.REGISTER) {
			register(session);
		}else if (msg.getAction() == WsAction.CLOSE) {
			processOnClose(session);
		}else if (msg.getAction() == WsAction.PRINT_FINISHED) {
			this.printData = null;
		}else if (msg.getAction() == WsAction.SEND) {  //sendin data to all registered users .. 

			if(msg.getDataType().isPrintData()) {
				//save data..
				this.printData = message;
			}
			for(UserSession u: userSessions) {				
				logger.debug("Sending to: " + u.getSession().getId());
				u.SendData(message );

			}
		}else if(msg.getAction() == WsAction.REQUEST_DATA) { // remote requesting data ... 
			//send any saved message if any:
			if(msg.getDataType() == DataType.PRINT_DATA && printData != null){
				//send print data if requested when registering.
				session.getAsyncRemote().sendText(printData);
			}

		}

	}

	private UserSession register(Session session) throws IOException {

		//check if user exist, if exist.. remove and replace session
		UserSession us = userSessions.stream().filter(u -> u.getSession().getId() == session.getId()).findAny().orElse(null);

		if (us != null) {
			us.setSession(session);
			logger.info("UserPool: " + userSessions.size());
		}else {			
			UserSession u = new UserSession(session);
			userSessions.add(u);
			logger.info("Adding new session Id: " + session.getId() +  "  UserPool: " + userSessions.size());
		}
		return us;

	}
	public void processOnClose(Session session) {
		try {
			UserSession us = userSessions.stream().filter(u -> u.getSession().getId() == session.getId()).findAny().orElse(null);

			if (us != null) {
				logger.info("Closing removing session: " + session.getId() + " Size: " + userSessions.size());
				userSessions.remove(us);
				session.close();
			}
		}catch(IOException e) {
			logger.error("Cannot process on close of session", e);
		}
	}
}
