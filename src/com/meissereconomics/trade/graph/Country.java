package com.meissereconomics.trade.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Random;

import com.meissereconomics.trade.flow.MaxFlow;

public class Country implements Comparable<Country> {
	
	public static final String ROW = "RoW";

	private final int countries;
	private final int number;
	private String name;
	private Nodes nodes;

	public Country(String name, int number, int countries) {
		assert name.length() > 0;
		assert number < 50; // wiod
		this.name = name;
		this.number = number;
		this.countries = countries;
		this.nodes = new Nodes(this);
	}
	
	public int getNumber(){
		return number;
	}

	public ArrayList<Node> getNodeList() {
		return nodes.getList();
	}
	
	public boolean hasNode(String industry){
		return nodes.contains(industry);
	}

	public Node getNode(String industry) {
		return nodes.get(industry);
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

	private int modcount;
	private double exports;

	public double getExports() {
		int mods = nodes.getModCount();
		if (mods != modcount) {
			exports = 0.0;
			for (Node n: getNodeList()){
				exports += n.getExports();
			}
		}
		return exports;
	}

	public double getConsumption() {
		return getNodeList().stream().filter(n -> n.isConsumption()).mapToDouble(Node::getInputs).sum();
	}

	public double calculateComposition(EFlowBendingMode mode, double consumptionPreference, double epsilon) {
		double diff = 0.0;
		for (Node n : getNodeList()) {
			diff = Math.max(diff, n.calculateComposition(getConsumptionNode(), mode, consumptionPreference, epsilon));
		}
		return diff;
	}

	public void mergeConsumption() {
		Node consumption = new Node(this, Node.CONSUMPTION_TYPES[0]);
		assert !nodes.contains(consumption.getIndustry());
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
		if (nodes.contains(category)) {
			return nodes.get(category).getOrigin();
		} else {
			return null;
		}
	}

	public double getReusedImports() {
		double tot = 0.0;
		for (Node n: getNodeList()){
			tot += n.getReusedImports();
		}
		return tot;
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
		}
	}
	
	public void merge(String n1, String n2){
		getNode(n1).absorb(getNode(n2), 0);
		nodes.remove(n2);
	}

	public void collapseSmallestSectors(int sectors) {
		PriorityQueue<Node> nodes = new PriorityQueue<>(new Comparator<Node>() {

			@Override
			public int compare(Node o1, Node o2) {
				return Double.compare(o1.getOutputsInclConsumption(), o2.getOutputsInclConsumption());
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
		}
	}
	
	public double getMaxProcessingTrade(){
		return new MaxFlow(this, true, true).calculateMaxFlow();
	}
	
	public double getMaxImportConsumption(){
		return new MaxFlow(this, true, false).calculateMaxFlow();
	}
	
	public double getMaxReusedImports(){
		return getMaxProcessingTrade();
	}
	
	public double getMinReusedImports(){
		double imports = getImports();
		return Math.max(0.0, imports - getMaxImportConsumption()); // to get rid of rounding errors that push value below 0
	}

	public Node getConsumptionNode() {
		return getNode(Node.CONSUMPTION_TYPES[0]);
	}

	public void deriveOrigins(EFlowBendingMode mode, double consumptionPreference, double epsilon) {
		double difference = 1.0;
		while (difference >= epsilon) {
			difference = calculateComposition(mode, consumptionPreference, epsilon * difference);
		}
	}

	public String getName() {
		return name;
	}

	public double getImportReuse() {
		return getReusedImports() / getExports();
	}

	public int getCountries() {
		return countries;
	}

	public boolean isRestOfTheWorld() {
		return name.equals(ROW);
	}

	public int getSectors() {
		return nodes.getNonConsumptionSectors();
	}

}
