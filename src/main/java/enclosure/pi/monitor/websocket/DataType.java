package enclosure.pi.monitor.websocket;

public enum DataType {

	PRINT_DATA, 
	PRINT_DONE,
	PRINT_TOTAL_TIME, 
	PRINTER_SERIAL_DATA_WEB,
	PRINTER_SERIAL_DATA_INIT, //serial init. so basically all serial data that is stored
	PRINTER_SERIAL_DATA_TO_BACKEND, 
	SERIAL_INIT_DATA,
	NONE;
	
	
	
	public   boolean isPrintData() {
		return this == PRINT_DATA || this == PRINT_DONE || this == PRINT_TOTAL_TIME;
	}
}
