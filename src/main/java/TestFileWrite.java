import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestFileWrite {

	
	public static void main(String args[]) throws IOException {
		Path filePath= Paths.get("c:\\temp\\PrinterData"+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")) + ".txt");
		String eventString = "String 1";

	    byte[] strToBytes = eventString.getBytes();

	    Files.write(filePath, strToBytes, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
	    
	    
	    Files.write(filePath, "bobob".getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
		
	}
}
