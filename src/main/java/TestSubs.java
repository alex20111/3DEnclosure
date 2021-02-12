import java.time.LocalDateTime;

public class TestSubs {

	public static void main(String[] args) {
		String dt = " T:22.50 /0.00 B:59.58 /60.00 @:0 B@:68 W:4";
		
		
		if (dt.contains("T:") && dt.contains("B:")) {
			String strSplit[] = dt.trim().split(" ");
			
			for (String s1: strSplit) {
				System.out.println("Sp: " + s1);
			}
			
			String nozzle = strSplit[0].substring(strSplit[0].indexOf("T:") + 2 , strSplit[0].length());
			String nozzleMax = strSplit[1].substring(strSplit[1].indexOf("/") + 1 , strSplit[1].length());
			String bed = strSplit[2].substring(strSplit[2].indexOf("B:") + 2 , strSplit[2].length());
			String bedMax = strSplit[3].substring(strSplit[3].indexOf("/") + 1 , strSplit[3].length());
			System.out.println("Nozzle: " + nozzle +  "  Bed: " + bed + " Nozzle max: " + nozzleMax + " bed max: " + bedMax);
		}
		
		
		System.out.println(LocalDateTime.now().toString());
		
		String newString = "SD printing byte 29868/231283";
		
		String subStr = newString.substring(newString.indexOf("byte") + 4, newString.length()).trim();
		String bytesRemaining[] = subStr.split("/");
		
		
		
		double completed = Double.parseDouble(bytesRemaining[0]);
		double total = Double.parseDouble(bytesRemaining[1]);
		double percentDbl = (completed / total) * 100;
		int percent = (int)percentDbl;
		
		System.out.println("SubStr: " + subStr +  " compled: " + completed + " total: " + total + " Percent: " + percent);
	

	}

}
