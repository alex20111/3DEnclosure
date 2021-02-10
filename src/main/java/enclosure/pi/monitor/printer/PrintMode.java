package enclosure.pi.monitor.printer;

public enum PrintMode {
	SD_PRINTING, PI_PRINTING, NOT_PRINTING;
	
	
	public boolean isPrinting() {
		return this == SD_PRINTING || this == PI_PRINTING;
	}
}
