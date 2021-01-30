import java.util.ArrayList;
import java.util.List;

public class TestString1 {

	private static String tempString = "";
	private static List<String> result = new ArrayList<>();;

	public static void main(String[] args) {
		//idea.. loop through the stru=ing and split all the <fhfh> into a list to string.. if < encountered without a > then save it in temp to be processed the next strign..


		String a = "<faf><5tt>";
		String b = "<faf><57t";
		String b1 = "5tt>";
		String c = "<faf><5tt><aa>";
		String c2 = "<aafaf><jdh2<aaaaa>";

		findStartEnd(a);
		findStartEnd(b);

		findStartEnd(b1);
		findStartEnd(c);
		findStartEnd(c2);

		String chara = ">";
		System.out.println(chara.toCharArray()[0]);
		
	}


	private static void findStartEnd(String str) {

		result.clear();

		boolean recvInProgress = false;
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < str.length() ; i++) {
			if (recvInProgress == true) {
				if (str.charAt(i) == '<') {
					//if start found again.. discardthe partial one and continue
					sb = new StringBuilder();
				}else if( str.charAt(i) == '>'){				    	 
					recvInProgress = false;
					result.add(sb.toString());
					sb = new StringBuilder();
					tempString = "";
				} else if (str.charAt(i) != '>') {
					sb.append(str.charAt(i));
				} 

			} else if (str.charAt(i) == '<') {
				recvInProgress = true;

			}else if (tempString.trim().length() > 0) {
				//					System.out.println("Adding to tempo : " + tempString + "   a " + sb.toString());
				recvInProgress = true;
				sb.append(tempString);
				sb.append(str.charAt(i));
			}
		}

		if (recvInProgress) {
			System.out.println("Appending: " + sb.toString());
			tempString = sb.toString();
		}


		System.out.println("Result: " + result);

	}



}
