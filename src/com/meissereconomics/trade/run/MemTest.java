package com.meissereconomics.trade.run;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.meissereconomics.trade.data.InputOutputGraph;
import com.meissereconomics.trade.data.OldWiodInputOutputGraph;

public class MemTest {
	
	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		System.out.println("Start");
		printMem();
		InputOutputGraph g = new OldWiodInputOutputGraph("data/wiot11_row_sep12.CSV");
		printMem();
		g.collapseRandomSectors(13, 1);
		printMem();
	}

	private static void printMem() {
		System.gc();
		long free = Runtime.getRuntime().freeMemory();
		long tot = Runtime.getRuntime().totalMemory();
		long occ = (tot - free)/1024/1024;
		System.out.println(occ + " MB used");
	}

}
