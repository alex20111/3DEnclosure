package enclosure.pi.monitor.arduino;

public class CleanedEvent implements Command{
	
	private String command = "";
	private String data  = "";
	
	
	public CleanedEvent(String event) {
		this.command = String.valueOf(event.charAt(0));
		this.data = event.substring(1, event.length() );
	}
	
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "CleanedEvent [command=" + command + ", data=" + data + "]";
	}
	

}
