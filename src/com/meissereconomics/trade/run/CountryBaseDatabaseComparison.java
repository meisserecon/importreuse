package com.meissereconomics.trade.run;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.meissereconomics.trade.data.InputOutputGraph;
import com.meissereconomics.trade.data.OldWiodInputOutputGraph;
import com.meissereconomics.trade.data.WiodInputOutputGraph;
import com.meissereconomics.trade.graph.Country;
import com.meissereconomics.trade.graph.EFlowBendingMode;

public class CountryBaseDatabaseComparison {

	private InputOutputGraph graph;
	private EFlowBendingMode mode = EFlowBendingMode.DEFAULT;

	public CountryBaseDatabaseComparison(InputOutputGraph graph, String country) {
		this.graph = graph;
	}

	public void compare(String country) throws FileNotFoundException, IOException {
		Country c = this.graph.getCountry(country);
		this.graph.deriveOrigins(mode, 0.0);
		System.out.println("Based on " + graph.toString() + " with " + c.getSectors() + " in " + c.getName());
		System.out.println("Global Leontief reuse: " + this.graph.getGlobalImportReuse());
		System.out.println(country + " Leontief reuse: " + c.getImportReuse());
		if (c.getSectors() > 1) {
			CountryPreferenceTest test = new CountryPreferenceTest(graph, country, 12345, 5);
			double bending = test.minimize(mode);
			graph.deriveOrigins(mode, bending);
			System.out.println("Global bent reuse: " + this.graph.getGlobalImportReuse());
			System.out.println(country + " bent reuse: " +c.getImportReuse());
		}
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		String country = "DEU";
		CountryBaseDatabaseComparison comp1 = new CountryBaseDatabaseComparison(new OldWiodInputOutputGraph(2008), country);
		comp1.compare(country);
		int sectors = comp1.graph.getSectors();
		comp1.graph.getCountry(country).collapseRandomSectors(133, 1);
		comp1.compare(country);
		comp1 = null;
		CountryBaseDatabaseComparison comp2 = new CountryBaseDatabaseComparison(new WiodInputOutputGraph(2008), country);
		comp2.compare(country);
		comp2.graph.getCountry(country).collapseRandomSectors(133, sectors);
		comp2.compare(country);
		comp2.graph.getCountry(country).collapseRandomSectors(133, 1);
		comp2.compare(country);
	}

}
