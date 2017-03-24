package com.meissereconomics.trade.run;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.meissereconomics.trade.EFlowBendingMode;
import com.meissereconomics.trade.data.InputOutputGraph;
import com.meissereconomics.trade.data.OldWiodInputOutputGraph;

public class SingleCountryRelief {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		EFlowBendingMode mode = EFlowBendingMode.IMPORTS_AWARE_MID;
		InputOutputGraph graph = new OldWiodInputOutputGraph(2007); // new USGraph(false); //
		CountryPreferenceTest test = new CountryPreferenceTest(graph, "DEU", 123135, 10);
		System.out.println("Printing individual runs for " + test.country + " in mode " + mode);
		double bending = test.minimize(mode);
		double[][] reuseOpt = test.calculateReuse();
		double staticBending = 0.0;
		test.setBending(mode, staticBending);
		double[][] reuseLeon = test.calculateReuse();
		int runs = reuseOpt[1].length;
		int sectors = reuseOpt.length;
		System.out.print("Bending\tRun");
		for (int sector = 1; sector <= sectors; sector++) {
			System.out.print("\t" + sector);
		}
		System.out.println();
		printTable(bending, reuseOpt, runs);
		printTable(staticBending, reuseLeon, runs);
	}

	private static void printTable(double string, double[][] reuseOpt, int runs) {
		for (int run = 0; run < runs; run++) {
			System.out.print(string + "\t" + (run + 1));
			for (int s = 0; s < reuseOpt.length; s++) {
				double[] current = reuseOpt[s];
				double reuse = current.length == 1 ? current[0] : current[run];
				System.out.print("\t" + reuse);
			}
			System.out.println();
		}
	}

}
