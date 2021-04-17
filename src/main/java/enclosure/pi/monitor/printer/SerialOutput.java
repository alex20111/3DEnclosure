package enclosure.pi.monitor.printer;

public class SerialOutput {

	private int recordNbr = -1;
	private String record = "";
	
	
	public SerialOutput(int nbr, String record) {
		this.recordNbr = nbr;
		this.record = record;
	}
	
	public String getRecord() {
		return record;
	}

	public int getRecordNbr() {
		return recordNbr;
	}

}
