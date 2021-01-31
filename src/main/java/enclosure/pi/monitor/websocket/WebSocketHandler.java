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
	
	public void processMessage(String message, Session session) throws IOException {
		
		SocketMessage msg = JsonIterator.deserialize(message, SocketMessage.class);
		
		if (msg.getAction() == WsAction.REGISTER) {
			register(session);
		}else if (msg.getAction() == WsAction.CLOSE) {
			closeSession(session);
		}else if (msg.getAction() == WsAction.SEND) {
			for(UserSession u: userSessions) {
				
					logger.debug("Sending to: " + session.getId());
					u.SendData( "bob");
				
			}
		}
		
	}
	
	private void register(Session session) throws IOException {

		//check if user exist, if exist.. remove and replace session
		UserSession us = userSessions.stream().filter(u -> u.getSession().getId() == session.getId()).findAny().orElse(null);

		if (us != null) {
			us.setSession(session);
//			us.addService(data);
//			us.SendData("{\"User\": \"User exist.. Updated\"}" );
			logger.info("UserPool: " + userSessions.size());
		}else {			
			UserSession u = new UserSession(session);
			userSessions.add(u);
			logger.info("Adding new session Id: " + session.getId() +  "  UserPool: " + userSessions.size());
		}
		

	}
	private void closeSession(Session session) throws IOException {
		UserSession us = userSessions.stream().filter(u -> u.getSession().getId() == session.getId()).findAny().orElse(null);
		
		if (us != null) {
			logger.info("Closing removing session: " + userSessions.size());
			userSessions.remove(us);
			session.close();
		}
	}
}
