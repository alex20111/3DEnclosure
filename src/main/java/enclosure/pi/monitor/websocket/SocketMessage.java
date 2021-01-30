package enclosure.pi.monitor.websocket;

public class SocketMessage {
	
	private WsAction action;
	private String message = "" ; // could contain an onject in JSON.
	
	public WsAction getAction() {
		return action;
	}
	public void setAction(WsAction action) {
		this.action = action;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

}
