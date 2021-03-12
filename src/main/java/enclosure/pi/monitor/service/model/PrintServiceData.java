package enclosure.pi.monitor.service.model;

import java.time.LocalDateTime;
import java.util.List;

public class PrintServiceData {

	private FileList printFile;
	private List<FileList> listFiles;
	private boolean printing = false;
	private boolean printCompleted = false;
	private boolean printerAborded = false;	
	private boolean printingModel = false; // that the printer is actually started printing the 3d model and not just the heating up.
	private boolean printPaused = false;
	
	private boolean printerConnected = false;
	private boolean autoPrinterShutdown = false;
	private boolean printerShutdownInProgress = false;
	
	//time/date display
	private int printTimeSeconds = -1;
	private String printStarted;
	private float bedTemp = -1.0f;
	private float bedTempMax = -1.0f;
	private float nozzleTemp = -1.0f;
	private float nozzleTempMax = -1.0f;	
	private int percentComplete = -1;
	
	//dashboard info
	private boolean extrFanOnAuto = false;
	private int extracFanRPM = -1;
	private int extracFanSpeed = -1;
	private String temperature = "";
	private boolean lightOn = false;
	private String airQualityCo2 = "";
	private String airQualityVoc = "";
		
	public PrintServiceData() {}	
	
	public boolean isExtrFanOnAuto() {
		return extrFanOnAuto;
	}
	public void setExtrFanOnAuto(boolean extrFanOnAuto) {
		this.extrFanOnAuto = extrFanOnAuto;
	}
	public int getExtracFanRPM() {
		return extracFanRPM;
	}
	public void setExtracFanRPM(int extracFanRPM) {
		this.extracFanRPM = extracFanRPM;
	}
	public int getExtracFanSpeed() {
		return extracFanSpeed;
	}
	public void setExtracFanSpeed(int extracFanSpeed) {
		this.extracFanSpeed = extracFanSpeed;
	}
	public String getTemperature() {
		return temperature;
	}
	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}
	public boolean isLightOn() {
		return lightOn;
	}
	public void setLightOn(boolean lightOn) {
		this.lightOn = lightOn;
	}
	public String getAirQualityVoc() {
		return airQualityVoc;
	}
	public void setAirQualityVoc(String airQualityVoc) {
		this.airQualityVoc = airQualityVoc;
	}
	public String getAirQualityCo2() {
		return airQualityCo2;
	}
	public void setAirQualityCo2(String airQualityCo2) {
		this.airQualityCo2 = airQualityCo2;
	}

	
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

	public boolean isPrintCompleted() {
		return printCompleted;
	}

	public void setPrintCompleted(boolean printCompleted) {
		this.printCompleted = printCompleted;
	}
	public void startPiPrinting(FileList file) {
		printerShutdownInProgress = false;
		printCompleted = false;
		printerAborded = false;
		printPaused = false;
		printTimeSeconds = -1;
		setPrinting(true);
		setPrintStarted(LocalDateTime.now().toString());
		setPercentComplete(-1);
		setPrintFile(file);
	}
	public void startSDPrinting(FileList file) {
		printerShutdownInProgress = false;
		printCompleted = false;
		printerAborded = false;
		printPaused = false;
		printTimeSeconds = -1;
		setPrinting(true);
		setPrintStarted(LocalDateTime.now().toString());
		setPercentComplete(0);
		setPrintFile(file);		
	}
	
	public void setPrintFinished() {
		printerAborded = false;
		printPaused = false;
		setPrintTimeSeconds(-1);
		setPrinting(false);
		printCompleted = true;
		setPrintStarted(null);
		printingModel = false;
		setPercentComplete(100);		
	}
	
	public void printAborded() {
		printerShutdownInProgress = false;
		printerAborded = true;
		printPaused = false;
		setPrintTimeSeconds(-1);
		setPrinting(false);
		printCompleted = false;
		printingModel = false;
		setPrintStarted(null);
	}

	public boolean isPrinterConnected() {
		return printerConnected;
	}

	public void setPrinterConnected(boolean printerConnected) {
		this.printerConnected = printerConnected;
	}

	public boolean isPrinterAborded() {
		return printerAborded;
	}

	public void setPrinterAborded(boolean printerAborded) {
		this.printerAborded = printerAborded;
	}

	public boolean isAutoPrinterShutdown() {
		return autoPrinterShutdown;
	}

	public void setAutoPrinterShutdown(boolean autoPrinterShutdown) {
		this.autoPrinterShutdown = autoPrinterShutdown;
	}

	public boolean isPrinterShutdownInProgress() {
		return printerShutdownInProgress;
	}

	public void setPrinterShutdownInProgress(boolean printerShutdownInProgress) {
		this.printerShutdownInProgress = printerShutdownInProgress;
	}

	public boolean isPrintingModel() {
		return printingModel;
	}

	public void setPrintingModel(boolean printingModel) {
		this.printingModel = printingModel;
	}

	public boolean isPrintPaused() {
		return printPaused;
	}

	public void setPrintPaused(boolean printPaused) {
		this.printPaused = printPaused;
	}

	@Override
	public String toString() {
		return "PrintServiceData [printFile=" + printFile + ", listFiles=" + listFiles + ", printing=" + printing
				+ ", printCompleted=" + printCompleted + ", printerConnected=" + printerConnected + ", printerAborded="
				+ printerAborded + ", autoPrinterShutdown=" + autoPrinterShutdown + ", printerShutdownInProgress="
				+ printerShutdownInProgress + ", printTimeSeconds=" + printTimeSeconds + ", printStarted="
				+ printStarted + ", bedTemp=" + bedTemp + ", bedTempMax=" + bedTempMax + ", nozzleTemp=" + nozzleTemp
				+ ", nozzleTempMax=" + nozzleTempMax + ", percentComplete=" + percentComplete + ", extrFanOnAuto="
				+ extrFanOnAuto + ", extracFanRPM=" + extracFanRPM + ", extracFanSpeed=" + extracFanSpeed
				+ ", temperature=" + temperature + ", lightOn=" + lightOn + ", airQualityCo2=" + airQualityCo2
				+ ", airQualityVoc=" + airQualityVoc + "]";
	}



}
