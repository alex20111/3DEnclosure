package enclosure.pi.monitor.service.model;

public class FileList {
	private String fileName = "";
	private String fileSize = "";
	private boolean fileFromPi = false;
	private boolean fileFromSd = false;
	
	
	public FileList(String name, long size, boolean fromPi, boolean fromSd) {
		this.fileName = name;
		this.fileSize = String.valueOf(size);
		this.fileFromPi = fromPi;
		this.fileFromSd = fromSd;
	}
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFileSize() {
		return fileSize;
	}
	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}

	public boolean isFileFromSd() {
		return fileFromSd;
	}

	public void setFileFromSd(boolean fileFromSd) {
		this.fileFromSd = fileFromSd;
	}

	public boolean isFileFromPi() {
		return fileFromPi;
	}

	public void setFileFromPi(boolean fileFromPi) {
		this.fileFromPi = fileFromPi;
	}

	@Override
	public String toString() {
		return "FileList [fileName=" + fileName + ", fileSize=" + fileSize + ", fileFromPi=" + fileFromPi
				+ ", fileFromSd=" + fileFromSd + "]";
	}


	
	
}
