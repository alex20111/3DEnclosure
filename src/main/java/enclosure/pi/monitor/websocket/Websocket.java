package enclosure.pi.monitor.websocket;

import java.io.IOException;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



@ServerEndpoint(value="/printerEvents/")
public class Websocket {

	private static final Logger logger = LogManager.getLogger(Websocket.class);
	
	private static WebSocketHandler handler;
	
	@OnOpen
	public void onOpen(Session session) {
		logger.debug("onOpen:: " + session.getId());        
		session.setMaxIdleTimeout(86400000);// the session last 24 hours or when the terminate connection is 
	}
	@OnClose
	public void onClose(Session session) {
		logger.debug("onClose:: " +  session.getId());		
//		WebSocketHandler.getInstance().processOnClose(session);
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		logger.debug("onMessage:: From=" + session.getId() + " Message=" + message);

		if(handler == null) {
			handler = new WebSocketHandler();
		}
		
		
		try {
			handler.processMessage(message, session);
		}catch(IOException e) {
			logger.error("on message error: " , e);
		}
	
	}
	
	 @OnMessage
     public void onPong(PongMessage pongMessage, Session session) {
		 logger.debug("onPong:: From=" + session.getId() + " Message=" + new String(pongMessage.getApplicationData().array() ) );
	 }
	
	

	@OnError
	public void onError(Throwable t) {
		logger.error("onError::" , t);
	}
}
