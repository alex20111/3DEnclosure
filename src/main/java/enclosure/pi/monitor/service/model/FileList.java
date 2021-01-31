package enclosure.pi.monitor.service.model;

public class FileList {
	private String fileName = "";
	private String fileSize = "";
	
	
	public FileList(String name, long size) {
		this.fileName = name;
		this.fileSize = String.valueOf(size);
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

	@Override
	public String toString() {
		return "FileList [fileName=" + fileName + ", fileSize=" + fileSize + "]";
	}
	
	
}
