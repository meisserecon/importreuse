package com.meissereconomics.trade.run;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.meissereconomics.trade.Country;
import com.meissereconomics.trade.data.InputOutputGraph;
import com.meissereconomics.trade.data.OldWiodInputOutputGraph;

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
		InputOutputGraph iograph = new OldWiodInputOutputGraph("data/wiot11_row_sep12.CSV");
		System.out.print("Sectors");
		for (Country c : iograph.getCountries()) {
			System.out.print("\t" + c.toString() + " Max\t" + c.toString() + " Min\t" + c.toString() + " Exports");
		}
		System.out.println();
		for (int level = OldWiodInputOutputGraph.SECTORS; level > 0; level--) {
			iograph.collapseRandomSectors(13, level);
			System.out.print(level);
			for (Country c : iograph.getCountries()) {
				System.out.print("\t" + c.getMaxReusedImports() + "\t" + c.getMinReusedImports() + "\t" + c.getExports());
			}
			System.out.println();
		}
		long deltaT = System.nanoTime() - t0;
		System.out.println("Run " + 0 + " completed after " + deltaT / 1000000 + "ms");
	}

}
