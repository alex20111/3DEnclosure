import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import enclosure.pi.monitor.printer.SerialOutput;

public class TestBlockingQueue {

	private static BlockingQueue<String> q = new ArrayBlockingQueue<>(100);


	public static void main(String args[]) throws InterruptedException, IOException {
		 Date s2 = new Date();
		File file = new File("C:\\Users\\ADMIN\\Desktop\\New folder (2)\\PrinterData2021-02-13-11-17-12.txt");
		
		List<SerialOutput> array = new ArrayList<>();
		StringBuilder s = new StringBuilder();
		
		InputStream is = new FileInputStream(file);
		int idx = 0;
		  try (BufferedReader br
			      = new BufferedReader(new InputStreamReader(is))) {
			        String line;
			        while ((line = br.readLine()) != null) {
			        	array.add(new SerialOutput(idx, line));
			        	idx++;
			        }
			    }

		 

		 for(SerialOutput s3: array) {
			 if (s3.getRecordNbr() == 9565) {
				 System.out.println("go: " + s3.getRecord());
			 }
		
		 }
		 Date e = new Date();
		 System.out.println("Count: " + (e.getTime()-s2.getTime()));
		 
		  
//		  
//		thread1();
//
//		Thread.sleep(5000);
//
//		thread2();

	}

	public static void thread1() {
		System.out.println("Thread1 start");

		new Thread(new Runnable() {

			@Override
			public void run() {

				int cnt = 0;
				while(cnt < 20) {
					try {
						q.add("index from thread A: " + cnt);

						cnt++;
						Thread.sleep(100);
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				System.out.println("Thread1 finished");
			}

		}).start();

	}

	public static void thread2() {
		System.out.println("Thread2 start");
		new Thread(new Runnable() {

			@Override
			public void run() {

				while(true) {

					try {
						String pooling = q.take();
						System.out.println("From thread 2:  " + pooling);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}


				}
			}

		}).start();
	}
}
