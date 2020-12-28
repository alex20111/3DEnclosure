package enclosure.pi.monitor.service.model;

public class Message {
	
	private String messageType = "";
	private String message = "";
	
	public Message(MessageType type, String message) {
		this.messageType = type.name();
		this.message = message;
	}
	
	public String getMessageType() {
		return messageType;
	}


	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}


	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}
	
	public enum MessageType{
		SUCCESS, WARN, ERROR;
	}


}
