package com.meissereconomics.seminar.flow;

import java.util.ArrayList;
import java.util.function.ObjDoubleConsumer;

import org.jgrapht.alg.flow.EdmondsKarpMaximumFlow;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import com.meissereconomics.seminar.Country;
import com.meissereconomics.seminar.Node;

public class MaxFlow {

	private Node source, target;
	private DefaultDirectedWeightedGraph<Node, DefaultWeightedEdge> graph;

	public MaxFlow(Country c, boolean importsAsSource, boolean exportsAsTarget) {
		graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		ArrayList<Node> list = c.getNodeList();
		if (importsAsSource) {
			this.source = new Node(c, "Imports");
			this.graph.addVertex(source);
		} else {
			this.source = new Node(c, "Value Creation");
			this.graph.addVertex(source);
		}
		if (exportsAsTarget) {
			this.target = new Node(c, "Exports");
			this.graph.addVertex(target);
		} else {
			this.target = list.get(0);
		}
		for (Node n : list) {
			graph.addVertex(n);
		}
		for (final Node n : list) {
			double weight = importsAsSource ? n.getImports() : n.getCreatedValue();
			graph.setEdgeWeight(graph.addEdge(source, n), weight);
			n.forEachLocalInputs(new ObjDoubleConsumer<Node>() {

				@Override
				public void accept(Node t, double value) {
					assert value >= 0.0;
					graph.setEdgeWeight(graph.addEdge(t, n), value);
				}
			});
			if (exportsAsTarget) {
				graph.setEdgeWeight(graph.addEdge(n, target), n.getExports());
			}
		}
	}

	public double calculateMaxFlow() {
		EdmondsKarpMaximumFlow<Node, DefaultWeightedEdge> flow = new EdmondsKarpMaximumFlow<>(graph, 0.00001);
		double max = flow.buildMaximumFlow(source, target).getValue();
		return max;
	}

}
