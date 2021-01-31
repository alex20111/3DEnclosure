package enclosure.pi.monitor.websocket;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jsoniter.output.JsonStream;

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
	public void sendMessage(SocketMessage message) {
		String msg = JsonStream.serialize(message);
		clientEndPoint.sendMessage(msg);
	}
	
	public static void main(String argsp[]) throws InterruptedException {
		System.out.println("Socket tester");
//		WebSocketClient.getInstance().sendMessage(new SocketMessage(WsAction.REGISTER, "RTEst"));
//		Thread.sleep(10000);
		WebSocketClient.getInstance().sendMessage(new SocketMessage(WsAction.SEND, "RTEswwwwaat"));
//		
		Thread.sleep(10000);
		WebSocketClient.getInstance().sendMessage(new SocketMessage(WsAction.SEND, "22222t"));
	
	}

}
