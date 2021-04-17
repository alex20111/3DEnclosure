package enclosure.pi.monitor.websocket;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jsoniter.output.JsonStream;

import home.websocket.WebSocketClientEndPoint;
import home.websocket.WebSocketClientEndPoint.MessageHandler;
import home.websocket.WebSocketException;

public class WebSocketClient {

	private static final Logger logger = LogManager.getLogger(WebSocketClient.class);

	private static WebSocketClient client;
	private  WebSocketClientEndPoint clientEndPoint;
	private boolean intialized = false;

	private WebSocketClient() {

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
		if(intialized) {
			clientEndPoint.sendMessage(message);
		}else {
			initialize();
		}
	}
	public void sendMessage(SocketMessage message) {
		if (intialized) {
			String msg = JsonStream.serialize(message);
			clientEndPoint.sendMessage(msg);
		}else {
			initialize();
		}
	}
	
	public void addHandler(MessageHandler handler) {
		clientEndPoint.addMessageHandler(handler);
	}


	private void initialize() {
		try {
			clientEndPoint = new WebSocketClientEndPoint(new URI("ws://localhost:8080/printerEvents/"), 86400000);

			//connect
			clientEndPoint.connect();
			intialized = true;
		} catch (URISyntaxException | WebSocketException e) {
			intialized = false;
			logger.error("WebSocketClient error", e);
		} 
	}

}
