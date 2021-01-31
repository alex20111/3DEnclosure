package enclosure.pi.monitor.service.model;

import java.util.List;

public class PrintServiceData {

	private String printFile = "";
	private List<FileList> listFiles;
	private boolean printing = false;
	
	public PrintServiceData() {}
	
	
	
	public String getPrintFile() {
		return printFile;
	}
	public void setPrintFile(String printFile) {
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
	

}
