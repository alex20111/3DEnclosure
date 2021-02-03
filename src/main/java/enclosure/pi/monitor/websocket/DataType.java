package enclosure.pi.monitor.websocket;

public enum DataType {

	PRINT_DATA,  PRINT_DONE, PRINT_TOTAL_TIME, NONE;
	
	
	
	public   boolean isPrintData() {
		return this == PRINT_DATA || this == PRINT_DONE || this == PRINT_TOTAL_TIME;
	}
}
