package enclosure.pi.monitor.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.websocket.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jsoniter.JsonIterator;
import com.jsoniter.output.JsonStream;

import enclosure.pi.monitor.websocket.UserSession.SessionType;

public class WebSocketHandler {

	private static final Logger logger = LogManager.getLogger(WebSocketHandler.class);

	//Angular will be registered in the user Session..
	//the server (java) will not be registerd , only send messages.

	private List<UserSession> userSessions = new ArrayList<UserSession>();
	private String printData = null;  //Socket message containing the print data.

	public void processMessage(String message, Session session) throws IOException {

		SocketMessage msg = JsonIterator.deserialize(message, SocketMessage.class);

		if (msg.getAction() == WsAction.REGISTER) {

			UserSession s = new UserSession(session);
			s.setType(SessionType.DASHBOARD);
			register(s);
		}else if (msg.getAction() == WsAction.REGISTER_FOR_SERIAL) {
			boolean isBackEnd =  Boolean.valueOf(msg.getMessage());
			
			UserSession s = new UserSession(session, Boolean.valueOf(msg.getMessage()));
			s.setType(SessionType.TERMINAL);
			register(s);
			logger.debug("backend: " + isBackEnd + " session id: " + session.getId());
			//send to the backend that you want the serial data
			if (!isBackEnd) { //we verify that it is not the backend requesting
				
				sendInitSerialData(s);
			}
			

		}else if (msg.getAction() == WsAction.CLOSE) {
			processOnClose(session);
		}else if (msg.getAction() == WsAction.SEND_TO_SERIAL_CONSOLE) {  
			

			if(msg.getDataType() == DataType.PRINTER_SERIAL_DATA_TO_USER) {
				UserSession us = findUser(msg.getAdditionalMessage()); //send to WEB that has register for that information. This contain session id
				us.SendData(message);///
			}else {
				for(UserSession u: userSessions) {	
					if (u.getType() == SessionType.TERMINAL) {
						if(msg.getDataType() == DataType.PRINTER_SERIAL_DATA_TO_BACKEND) {
							if (u.isSerialConsoleMaster()) {
								u.SendData(message);
								break;
							}
						}else if(msg.getDataType() == DataType.PRINTER_SERIAL_DATA_WEB) {
							if (!u.isSerialConsoleMaster()) {
								u.SendData(message);
							}
						}
					}
				}
			}
		}

		else if (msg.getAction() == WsAction.SEND) {  //sendin data to all registered users .. 

			if(msg.getDataType().isPrintData()) {
				//save data..
				this.printData = message;
			}
			for(UserSession u: userSessions) {	
				if (u.getType() == SessionType.DASHBOARD) {
				//				logger.debug("Sending to: " + u.getSession().getId());
					u.SendData(message );
				}
			}
		}else if(msg.getAction() == WsAction.REQUEST_DATA) { // remote requesting data ... 
			//send any saved message if any:
			if(msg.getDataType() == DataType.PRINT_DATA && printData != null){
				//send print data if requested when registering.
				session.getAsyncRemote().sendText(printData);
			}

		}

	}

	private UserSession register(UserSession uSession) throws IOException {

		//check if user exist, if exist.. remove and replace session
		UserSession us =findUser(uSession.getSession().getId());

		if (us != null) {
			us.setSession(uSession.getSession());
			logger.info("user found refreshing. UserPool: " + userSessions.size());
		}else {			

			userSessions.add(uSession);
			logger.info("Adding new session Id: " + uSession.getSession().getId() +  "  UserPool: " + userSessions.size());
		}
		return us;

	}
	//this will send the initial serial data from the printer when asking to register.
	private void sendInitSerialData(UserSession session) throws IOException {
		for(UserSession u: userSessions) {			
			if (u.isSerialConsoleMaster()) {	
				SocketMessage s  = new SocketMessage(WsAction.REGISTER_FOR_SERIAL, DataType.PRINTER_SERIAL_DATA_TO_BACKEND, session.getSession().getId());
				u.SendData(JsonStream.serialize(s));
				logger.debug("sendInitSerialData from: " + session.getSession().getId()  + "  To: " + u.getSession().getId());
				break;
			}
		}

	}
	public void processOnClose(Session session) {
		try {
			UserSession us = findUser(session.getId());

			if (us != null) {
				logger.info("Closing removing session: " + session.getId() + " Size: " + userSessions.size());
				userSessions.remove(us);
				session.close();
			}
		}catch(IOException e) {
			logger.error("Cannot process on close of session", e);
		}
	}

	private UserSession findUser(String id) {
		return userSessions.stream().filter(u -> u.getSession().getId().equalsIgnoreCase(id)).findAny().orElse(null);
	}
}
