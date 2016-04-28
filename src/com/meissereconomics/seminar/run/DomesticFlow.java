package com.meissereconomics.seminar.run;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.meissereconomics.seminar.Country;
import com.meissereconomics.seminar.InputOutputGraph;

/**
 * Running this script should produce output "DomesticFlow.out", a table showing the maximum possible
 * consumption of value of domestic origin. It uses the Edmonds-Karp algorithm to calculate the maximum
 * flow from an artificial "local value creation" node to domestic consumption.
 * 
 * Unfortunately, the network is too well-connected to impose any meaningful constraints on maximum flow.
 * For Luxembourg, maximum flow constrains consumption of locally produced value by merely 5%, and only 1%
 * at average world-wide.
 */
public class DomesticFlow {

	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		long t0 = System.nanoTime();
		InputOutputGraph iograph = new InputOutputGraph("data/wiot05_row_apr12.CSV");
		System.out.print("Level");
		for (Country c : iograph.getCountries()) {
			System.out.print("\t" + c.toString());
		}
		System.out.println();
		for (int level = InputOutputGraph.SECTORS; level > 0; level--) {
			iograph.collapseRandomSectors(13, level);
			System.out.print(level);
			for (Country c : iograph.getCountries()) {
				System.out.print("\t" + c.getMaxDomesticFlow(true));
			}
			System.out.println();
		}
		long deltaT = System.nanoTime() - t0;
		System.out.println("Run " + 0 + " completed after " + deltaT / 1000000 + "ms");
	}

}
