package com.meissereconomics.trade.analysis;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.meissereconomics.trade.data.InputOutputGraph;
import com.meissereconomics.trade.data.WiodInputOutputGraph;
import com.meissereconomics.trade.flow.Optimizer;
import com.meissereconomics.trade.graph.Country;
import com.meissereconomics.trade.graph.EFlowBendingMode;
import com.meissereconomics.trade.util.Table;

public class GlobalReuseHistory {

	private Table table;

	public GlobalReuseHistory() {
		this.table = new Table("Year", "Country");
	}

	public void printStats(InputOutputGraph graph) {
		this.table.setCurrent("Year", Integer.toString(graph.getYear()));
		graph.deriveOrigins(EFlowBendingMode.DEFAULT, 0.0);
		for (Country c : graph.getCountries()) {
			table.setCurrent("Country", c.getName());
			table.include("Imports", c.getImports());
			table.include("Value Added", c.getCreatedValue());
			table.include("Exports", c.getExports());
			table.include("Consumption", c.getConsumption());
			table.include("Leontief reuse", c.getImportReuse());
		}
		Optimizer opt = new Optimizer(graph, 13423, 5);
		for (Country c : graph.getCountries()) {
			double bending = opt.minimizeError(c.getName(), EFlowBendingMode.DEFAULT);
			table.include("Processing Trade Propensity", bending);
			c.deriveOrigins(EFlowBendingMode.DEFAULT, bending, 0.0005);
			table.include("Bent reuse", c.getImportReuse());
		}
		table.printAndFlush(false);
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		int firstYear = 2000;
		int lastYear = 2012;
		GlobalReuseHistory history = new GlobalReuseHistory();
		System.out.println("Global reuse history with new WIOD and 5 runs");
		for (int year = firstYear; year < lastYear; year++) {
			history.printStats(new WiodInputOutputGraph(year));
		}
	}

}
