package enclosure.pi.monitor.service.model;


import java.time.LocalTime;
import java.util.List;

public class PrintServiceData {

	private FileList printFile;
	private List<FileList> listFiles;
	private boolean printing = false;
	
	//time/date display
	private int printTimeSeconds = -1;
	private String printStarted;
	private float bedTemp = -1.0f;
	private float bedTempMax = -1.0f;
	private float nozzleTemp = -1.0f;
	private float nozzleTempMax = -1.0f;	
	private boolean printerBusy = false;	
	private int percentComplete = -1;
	private LocalTime lastUpdate;
	
	public PrintServiceData() {}	
	
	public FileList getPrintFile() {
		return printFile;
	}
	public void setPrintFile(FileList printFile) {
		this.printFile = printFile;
	}
	public List<FileList> getListFiles() {
		return listFiles;
	}
	public void setListFiles(List<FileList> listFiles) {
		this.listFiles = listFiles;
	}
	public boolean isPrinting() {
		return printing;
	}
	public void setPrinting(boolean printing) {
		this.printing = printing;
	}

	public int getPrintTimeSeconds() {
		return printTimeSeconds;
	}

	public void setPrintTimeSeconds(int printTimeSeconds) {
		this.printTimeSeconds = printTimeSeconds;
	}

	public float getBedTemp() {
		return bedTemp;
	}

	public void setBedTemp(float bedTemp) {
		this.bedTemp = bedTemp;
	}

	public float getNozzleTemp() {
		return nozzleTemp;
	}

	public void setNozzleTemp(float nozzleTemp) {
		this.nozzleTemp = nozzleTemp;
	}

	public boolean isPrinterBusy() {
		return printerBusy;
	}

	public void setPrinterBusy(boolean printerBusy) {
		this.printerBusy = printerBusy;
	}

	public LocalTime getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(LocalTime lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public float getBedTempMax() {
		return bedTempMax;
	}

	public void setBedTempMax(float bedTempMax) {
		this.bedTempMax = bedTempMax;
	}

	public float getNozzleTempMax() {
		return nozzleTempMax;
	}

	public void setNozzleTempMax(float nozzleTempMax) {
		this.nozzleTempMax = nozzleTempMax;
	}

	public String getPrintStarted() {
		return printStarted;
	}

	public void setPrintStarted(String printStarted) {
		this.printStarted = printStarted;
	}

	public int getPercentComplete() {
		return percentComplete;
	}

	public void setPercentComplete(int percentComplete) {
		this.percentComplete = percentComplete;
	}

	@Override
	public String toString() {
		return "PrintServiceData [printFile=" + printFile + ", listFiles=" + listFiles + ", printing=" + printing
				+ ", printTimeSeconds=" + printTimeSeconds + ", printStarted=" + printStarted + ", bedTemp=" + bedTemp
				+ ", bedTempMax=" + bedTempMax + ", nozzleTemp=" + nozzleTemp + ", nozzleTempMax=" + nozzleTempMax
				+ ", printerBusy=" + printerBusy + ", lastUpdate=" + lastUpdate + "]";
	}



	

}
