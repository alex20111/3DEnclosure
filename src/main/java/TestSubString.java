
public class TestSubString {

	
	public static void main(String args[]) {
		String ti = ";TIME:738.2323";
		
		
		Double i = Double.parseDouble(ti.substring(ti.indexOf(":") + 1));
		
		System.out.println("i: " + i);
		
		int seconds =  (int)Math.round(i);
		
        int p1 = seconds % 60;
        int p2 = seconds / 60;
        int p3 = p2 % 60;
        p2 = p2 / 60;
        System.out.print( p2 + ":" + p3 + ":" + p1);
		System.out.print("\n");
	}
}
