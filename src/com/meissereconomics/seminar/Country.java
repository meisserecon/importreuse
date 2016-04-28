package com.meissereconomics.seminar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Random;

import com.meissereconomics.seminar.flow.MaxFlow;
import com.meissereconomics.seminar.util.InstantiatingHashmap;

public class Country implements Comparable<Country> {

	private String name;
	private InstantiatingHashmap<String, Node> nodes;
	private ArrayList<Node> nodeList;

	public Country(String name) {
		assert name.length() > 0;
		this.name = name;
		this.nodes = new InstantiatingHashmap<String, Node>() {

			@Override
			protected Node createValue(String key) {
				Country.this.nodeList = null;
				return new Node(Country.this, key);
			}
		};
	}

	public ArrayList<Node> getNodeList() {
		if (nodeList == null) {
			nodeList = new ArrayList<>();
			for (Node n : nodes.values()) {
				nodeList.add(n);
				if (n.isConsumption()) {
					int pos = nodeList.size() - 1;
					nodeList.set(pos, nodeList.get(0));
					nodeList.set(0, n);
				}
			}
		}
		return nodeList;
	}

	public Node getNode(String industry) {
		return nodes.obtain(industry);
	}

	public double getImports() {
		return getNodeList().stream().mapToDouble(Node::getImports).sum();
	}

	public double getCreatedValue() {
		double sum = 0.0;
		ArrayList<Node> nodes = getNodeList();
		for (int i = 1; i < nodes.size(); i++) {
			sum += nodes.get(i).getCreatedValue();
		}
		return sum;
	}

	public double getExports() {
		return getNodeList().stream().mapToDouble(Node::getExports).sum();
	}

	public double getConsumption() {
		return getNodeList().stream().filter(n -> n.isConsumption()).mapToDouble(Node::getInputs).sum();
	}

	public double calculateComposition(EFlowBendingMode mode, double consumptionPreference) {
		double diff = 0.0;
		for (Node n : getNodeList()) {
			diff = Math.max(diff, n.calculateComposition(getConsumptionNode(), mode, consumptionPreference));
		}
		return diff;
	}

	public void updateComposition() {
		for (Node n : getNodeList()) {
			n.updateComposition();
		}
	}

	public void mergeConsumption() {
		Node consumption = new Node(this, Node.CONSUMPTION_TYPES[0]);
		assert!nodes.containsKey(consumption.getIndustry());
		Iterator<Node> iter = nodes.values().iterator();
		double exp = getExports();
		while (iter.hasNext()) {
			Node n = iter.next();
			if (n.isConsumption()) {
				consumption.absorb(n, -1);
				iter.remove();
			}
			assert Math.abs(exp - getExports()) < 0.01;
		}
		this.nodes.put(Node.CONSUMPTION_TYPES[0], consumption);
		this.nodeList = null;
	}

	public String getStats() {
		return name + " has " + nodes.size() + " industries, imports " + getImports() + ", exports " + getExports() + ", and consumes: " + getConsumption();
	}

	@Override
	public int compareTo(Country o) {
		return name.compareTo(o.name);
	}

	public String toString() {
		return name;
	}

	public void printDetailedStats() {
		ArrayList<Node> nodes = new ArrayList<>(getNodeList());
		Collections.sort(nodes, new Comparator<Node>() {

			@Override
			public int compare(Node o1, Node o2) {
				return Double.compare(o1.getCreatedValue(), o1.getCreatedValue());
			}
		});
		for (Node n : nodes) {
			System.out.println(n.getStats());
		}
	}

	public void turnNegativeLinks() {
		for (Node n : this.nodes.values()) {
			n.turnNegativeInputs();
		}
	}

	public Composition getOriginOfConsumption() {
		return getOriginOf(Node.CONSUMPTION_TYPES[0]);
	}

	public Composition getOriginOf(String category) {
		if (nodes.containsKey(category)) {
			return nodes.get(category).getOrigin();
		} else {
			return null;
		}
	}

	public double getReusedImports() {
		return getNodeList().stream().mapToDouble(Node::getReusedImports).sum();
	}

	public void collapseRandomSectors(int seed, int sectors) {
		Random rand = new Random(seed);
		ArrayList<Node> nodes = new ArrayList<>(getNodeList());
		nodes.remove(0); // remove consumption
		while (nodes.size() > sectors) {
			int level = nodes.size() - 1;
			Node n2 = nodes.remove(rand.nextInt(nodes.size()));
			Node n1 = nodes.remove(rand.nextInt(nodes.size()));
			n1.absorb(n2, level);
			nodes.add(n1);
			this.nodes.remove(n2.getIndustry());
			this.nodeList = null;
		}
	}

	public void collapseSmallestSectors(int sectors) {
		PriorityQueue<Node> nodes = new PriorityQueue<>(new Comparator<Node>() {

			@Override
			public int compare(Node o1, Node o2) {
				return Double.compare(o1.getOutputs(), o2.getOutputs());
			}
		});
		for (Node n : getNodeList()) {
			if (!n.isConsumption()) {
				nodes.add(n);
			}
		}
		while (nodes.size() > sectors) {
			int level = nodes.size() - 1;
			Node smallest = nodes.poll();
			Node secondSmallest = nodes.poll();
			secondSmallest.absorb(smallest, level);
			nodes.add(secondSmallest);
			this.nodes.remove(smallest.getIndustry());
			this.nodeList = null;
		}
	}

	public double getMaxDomesticFlow(boolean imports) {
		return new MaxFlow(this, imports).calculateMaxFlow();
	}
	
	public Node getConsumptionNode() {
		return getNode(Node.CONSUMPTION_TYPES[0]);
	}

	public void deriveOrigins(EFlowBendingMode mode, double consumptionPreference) {
		double difference = 1.0;
		while (difference >= 0.001) {
			difference = calculateComposition(mode, consumptionPreference);
			updateComposition();
		}
	}

	public String getName() {
		return name;
	}

	public double getImportReuse() {
		return getReusedImports() / getExports();
	}

}
