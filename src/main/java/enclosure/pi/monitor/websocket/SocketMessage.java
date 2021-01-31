package enclosure.pi.monitor.websocket;

public class SocketMessage {
	
	private WsAction action;
	private String dataType = ""; // identify the data type tat is to be sent. Ex: pintting info...
	private String message = "" ; // could contain an onject in JSON.
	
	public SocketMessage() {}
	
	public SocketMessage(WsAction action, String message) {
		this.action = action;
		this.message = message;
	}
	
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

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	@Override
	public String toString() {
		return "SocketMessage [action=" + action + ", message=" + message + "]";
	}

}
