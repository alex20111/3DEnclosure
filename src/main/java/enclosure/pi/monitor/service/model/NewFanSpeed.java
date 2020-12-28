package enclosure.pi.monitor.service.model;

public class NewFanSpeed {

	public static void main(String[] args) {
		
		
		int speed = 0 ;
		int max = 255;
		int low = 0; 
		
//		double result = ((double) speed /100 * 255 );
		
//		double result = ((double)val / max) * 100;
		
		while(speed < 110) {
			double result = ((double) speed /100 * 255 );
			int r = (int)result;
			System.out.println("result: " + result + "  r: " + r);
			speed += 10;
		}
	
		String g = "{\"speed\":\"20\"}";

		System.out.println("New : " + g.substring(10, g.length() -2));
		
		String fff = "r0";
		if (fff.startsWith("r")) {
			System.out.println("Starting with R. " + fff.substring(1,fff.length()));
		}
		
	}

}
 