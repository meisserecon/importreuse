package com.meissereconomics.trade.analysis;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.meissereconomics.trade.data.InputOutputGraph;
import com.meissereconomics.trade.data.OldWiodInputOutputGraph;
import com.meissereconomics.trade.data.WiodInputOutputGraph;
import com.meissereconomics.trade.flow.Optimizer;
import com.meissereconomics.trade.graph.Country;
import com.meissereconomics.trade.graph.EFlowBendingMode;
import com.meissereconomics.trade.graph.Node;
import com.meissereconomics.trade.util.Table;

public class NodeStats {

	private InputOutputGraph[] graphs;

	public NodeStats(InputOutputGraph... graphs) {
		this.graphs = graphs;
	}

	public void printSectorRanking(String country) {
		Table t = new Table("Graph", "Country", "Sector");
		t.setCurrent("Country", country);
		for (InputOutputGraph graph : graphs) {
			t.setCurrent("Graph", graph.toString());
			Country c = graph.getCountry(country);
			List<Node> nodes = c.getNodeList();
			for (Node n : nodes) {
				t.setCurrent("Sector", n.getIndustry());
				t.include("In", n.getInputs());
				t.include("Imports", n.getImports());
				t.include("Out", n.getOutputsInclConsumption());
				t.include("Exports", n.getExports());
				t.include("Value", n.getCreatedValue());
				t.include("Consumption", n.getDomesticConsumption());
			}
		}
		t.printAll(false);
	}

	public void printCountryStats(Table t, String country) {
		t.setCurrent("Country", country);
		for (InputOutputGraph graph : graphs) {
			t.setCurrent("Graph", graph.toString());
			Country c = graph.getCountry(country);
			t.include("Imports", c.getImports());
			t.include("Value Added", c.getCreatedValue());
			t.include("Exports", c.getExports());
			t.include("Consumption", c.getConsumption());
		}
		t.printAndFlush(false);
	}

	public void printGraphStats(InputOutputGraph graph, Table t) {
		t.setCurrent("Graph", graph.toString());
		for (Country c : graph.getCountries()) {
			t.setCurrent("Country", c.getName());
			t.include("Imports", c.getImports());
			t.include("Value Added", c.getCreatedValue());
			t.include("Exports", c.getExports());
			t.include("Consumption", c.getConsumption());
			c.deriveOrigins(EFlowBendingMode.DEFAULT, 0.0, 0.0005);
			t.include("Leontief reuse", c.getImportReuse());
			Optimizer opt = new Optimizer(graph, c.getName(), 13423, 5, false);
			double bending = opt.minimizeError(EFlowBendingMode.DEFAULT);
			t.include("Processing Trade Propensity", bending);
			c.deriveOrigins(EFlowBendingMode.DEFAULT, bending, 0.0005);
			t.include("Bent reuse", c.getImportReuse());
			t.printAndFlush(false);
		}
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		int firstYear = 2000;
		int lastYear = 2012;
		Table t = new Table("Graph", "Country");
		for (int year = firstYear; year < lastYear; year++) {
			new NodeStats(new OldWiodInputOutputGraph(year)).printCountryStats(t, "DEU");
			new NodeStats(new WiodInputOutputGraph(year)).printCountryStats(t, "DEU");
		}
	}

}
