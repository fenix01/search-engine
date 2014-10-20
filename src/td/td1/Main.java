package td.td1;

import java.util.ArrayList;

public class Main {

	public static void main(String[] args) {
		ArrayList<Integer> a1 = new ArrayList<>();
		ArrayList<MyInt> a2 = new ArrayList<>();
		
		for (int i = 0; i<10000; i++){
			a1.add(1);
			a2.add(new MyInt((short) 1));
		}
	}

}
