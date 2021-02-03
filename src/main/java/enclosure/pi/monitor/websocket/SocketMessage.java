package enclosure.pi.monitor.websocket;

public class SocketMessage {
	
	private WsAction action;
	private DataType dataType = DataType.NONE; // identify the data type tat is to be sent. Ex: pintting info...
	private String message = "" ; // could contain an onject in JSON.
	
	public SocketMessage() {}
	
	public SocketMessage(WsAction action, String message) {
		this.action = action;
		this.message = message;
	}
	
	public SocketMessage(WsAction action, DataType type, String message) {
		this.action = action;
		this.dataType = type;
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
	public DataType getDataType() {
		return dataType;
	}

	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}
	
	public boolean isPrintRelated() {
		return this.dataType == DataType.PRINT_DATA;
	}
	

	@Override
	public String toString() {
		return "SocketMessage [action=" + action + ", dataType=" + dataType + ", message=" + message + "]";
	}



}
