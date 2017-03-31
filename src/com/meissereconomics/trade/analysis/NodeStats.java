package com.meissereconomics.trade.analysis;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.meissereconomics.trade.data.InputOutputGraph;
import com.meissereconomics.trade.data.OldWiodInputOutputGraph;
import com.meissereconomics.trade.data.WiodInputOutputGraph;
import com.meissereconomics.trade.graph.Country;
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

	public static void main(String[] args) throws FileNotFoundException, IOException {
		int year = 2007;
		NodeStats stats = new NodeStats(new WiodInputOutputGraph(year), new OldWiodInputOutputGraph(year));
		stats.printSectorRanking("DEU");
	}

}
