package com.meissereconomics.seminar.split;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.meissereconomics.seminar.InputOutputGraph;

public class SplitDataWriter {

	private SplitCollection collection;

	public SplitDataWriter() {
		this.collection = new SplitCollection();
	}

	public void fill(int seed) throws FileNotFoundException, IOException {
		InputOutputGraph iograph = new InputOutputGraph("data/wiod-2005.CSV");
		for (int level = InputOutputGraph.SECTORS; level > 0; level--) {
			iograph.collapseRandomSectors(seed * 31, level, collection);
		}
	}

	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		// System.out.println("Run\tSectors\tReuse");
		long t0 = System.nanoTime();
		SplitDataWriter cor = new SplitDataWriter();
		for (int run = 0; run < 1; run++) {
			cor.fill(run * 31);
		}
		cor.collection.printAll();
		long deltaT = System.nanoTime() - t0;
//		System.out.println("Took " + deltaT / 1000000 + "ms");
	}

}
