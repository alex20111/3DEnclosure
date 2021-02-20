package enclosure.pi.monitor.arduino;


import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ArduinoSerialEvent implements Command {
	
	private static final Logger logger = LogManager.getLogger(ArduinoSerialEvent.class);

	private boolean commandComplete = false;
	private String command = "";
	
	//temp storage
	private String  tempString = "";
	
	//values

	private String output = "";
	private List<CleanedEvent> outputs = new ArrayList<>();
	
	public ArduinoSerialEvent() {
		
	}
	
	public void processEvent(String event) {
		 outputs = new ArrayList<>();
		 
		 boolean recvInProgress = false;
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < event.length() ; i++) {
				if (recvInProgress == true) {
					if (event.charAt(i) == START_MARKER.toCharArray()[0]) {
						//if start found again.. discardthe partial one and continue
						sb = new StringBuilder();
					}else if( event.charAt(i) == END_MARKER.toCharArray()[0]	  ){			  	 
						recvInProgress = false;
						outputs.add(new CleanedEvent(sb.toString()));
						sb = new StringBuilder();
						tempString = "";
					} else if (event.charAt(i) != END_MARKER.toCharArray()[0]) {
						sb.append(event.charAt(i));
					} 

				} else if (event.charAt(i) == START_MARKER.toCharArray()[0]) {
					recvInProgress = true;

				}else if (tempString.trim().length() > 0) {
					recvInProgress = true;
					sb.append(tempString);
					sb.append(event.charAt(i));
				}
			}

			if (recvInProgress) {
				tempString = sb.toString();
			}

	}
	
	
	
	
	public boolean isCommand(String command) {
		return this.command.equals(command);
	}
	
	public boolean isCommandComplete() {
		return commandComplete;
	}
	public String getOutput() {
		return output;
	}

	public List<CleanedEvent> getOutputs(){
		return this.outputs;
	}





}
