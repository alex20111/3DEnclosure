package enclosure.pi.monitor.websocket;

public enum DataType {

	PRINT_DATA, 
	PRINT_DONE,
	PRINT_TOTAL_TIME, 
	PRINTER_SERIAL_DATA_WEB,
	PRINTER_SERIAL_DATA_TO_USER, //send requested serial data to user
	PRINTER_SERIAL_DATA_TO_BACKEND,
	PRINTER_SERIAL_COMMAND,
	SERIAL_INIT_DATA,
	NONE;
	
	
	
	public   boolean isPrintData() {
		return this == PRINT_DATA || this == PRINT_DONE || this == PRINT_TOTAL_TIME;
	}
}
