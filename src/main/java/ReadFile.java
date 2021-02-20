import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ReadFile {

	public static void main(String[] args) throws FileNotFoundException, IOException {
//		C:\Users\ADMIN\Desktop\New folder (2)
		
		
		Path filePath= Paths.get("C:\\Users\\ADMIN\\Desktop\\3d printer\\98. custom models\\CCR6_3DBenchy.gcode");
		
		
		File f = filePath.toFile();
		
		 long totalLength = f.length();
	        double lengthPerPercent = 100.0 / totalLength;
	        long readLength = 0;
		
	        long start = new Date().getTime();
		try(BufferedReader objReader = new BufferedReader(new FileReader(f))){

			String str;
			while ((str = objReader.readLine()) != null) {
				
				int len = str.length() + 2;
				readLength += len;
				
				int res = (int)Math.round(readLength * lengthPerPercent);
				
//				System.out.println(Math.round(readLength * lengthPerPercent));
				
			}
		}
		long ens = new Date().getTime();
		
		System.out.println("Time : " + ( ens - start));
	}

}
