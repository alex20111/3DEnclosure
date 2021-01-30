package enclosure.pi.monitor.websocket;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import home.websocket.WebSocketClientEndPoint;
import home.websocket.WebSocketException;

public class WebSocketClient {
	
	private static final Logger logger = LogManager.getLogger(WebSocketClient.class);
	
	private static WebSocketClient client;
	private  WebSocketClientEndPoint clientEndPoint;
	
	private WebSocketClient() {
		try {
			clientEndPoint = new WebSocketClientEndPoint(new URI("ws://localhost:8080/printerEvents/"), 86400000);
			
			//connect
			clientEndPoint.connect();
		} catch (URISyntaxException | WebSocketException e) {
			logger.error("WebSocketClient error", e);
		}  
	}
	
	public static WebSocketClient getInstance() {
		if (client == null) {
			synchronized (WebSocketClient.class) {
				if(client == null) {
//					logger.info( "arduinoHandler initialized");
					client = new WebSocketClient();
				}
			}
		}
		
		return client;
	}
	
	public void sendMessage(String message) {
		clientEndPoint.sendMessage(message);
	}

}