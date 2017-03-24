package com.meissereconomics.trade.util;

public class Timer {
	
	private long t0 = System.nanoTime();

	public void time(String string) {
		long t1 = System.nanoTime();
		System.out.println((t1 - t0)/1000000 + "ms: " + string);
		t0 = t1;
	}

}
