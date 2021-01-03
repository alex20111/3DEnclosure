package enclosure.pi.monitor.arduino;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ArduinoSerialEvent implements Command {
	
	private static final Logger logger = LogManager.getLogger(ArduinoSerialEvent.class);

	private boolean commandComplete = false;
	private String command = "";
	
	//temp storage
	private StringBuilder tempStorage = new StringBuilder();
	
	//values

	private String output = "";
	
	public ArduinoSerialEvent() {
		
	}
	
	
	public synchronized void translateReceivedEvent(String receivedEvent) throws IllegalArgumentException{		

		commandComplete = false;
		command  = "";

//		String cmd = NONE;
		logger.debug( "receivedEvent: " + receivedEvent + ". TempStorage: " + (tempStorage != null? tempStorage.toString(): "Empty" ) );

		if (receivedEvent.startsWith(START_MARKER) && receivedEvent.endsWith(END_MARKER)){
			if (tempStorage != null && tempStorage.length() > 0){ //reset if we have anything in the temp
				logger.debug("resetting temp storage: " + tempStorage);
				tempStorage = new StringBuilder();
			}
			populateFromCommand(receivedEvent);			
		}else if(receivedEvent.startsWith(START_MARKER) && !receivedEvent.endsWith(END_MARKER)){
			//this means that we received the start but not the end tag. Store it into the temp variable
			logger.debug("receivedCmd partial serial info.. ");

			if (tempStorage != null && tempStorage.length() > 0){
				throw new IllegalArgumentException("Problem, temp storage not null. It should be empty. receivedCmd: " + receivedEvent + " - trempStorage: " + tempStorage);
			}
			tempStorage.append(receivedEvent);
		}else if (!receivedEvent.startsWith(START_MARKER) && receivedEvent.endsWith(END_MARKER)){
			logger.debug("receivedCmd end of partial serial info.. ");
			tempStorage.append(receivedEvent);
			populateFromCommand(tempStorage.toString());	
			tempStorage = new StringBuilder();
		}else{			
			throw new IllegalArgumentException("Problem processing received command. receivedEvent: " + receivedEvent);
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


	private void populateFromCommand(String cmdString) {
	
		output  = cmdString.substring(2, cmdString.indexOf(END_MARKER) );
		command = String.valueOf(cmdString.charAt(1));

		
		commandComplete = true;
	}




}
