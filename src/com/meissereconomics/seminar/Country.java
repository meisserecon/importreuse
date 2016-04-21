package com.meissereconomics.seminar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.function.BiConsumer;

import com.meissereconomics.seminar.util.InstantiatingHashmap;

import net.openhft.koloboke.function.DoubleDoubleConsumer;

public class Country implements Comparable<Country> {

	private String name;
	private InstantiatingHashmap<String, Node> nodes;

	public Country(String name) {
		assert name.length() > 0;
		this.name = name;
		this.nodes = new InstantiatingHashmap<String, Node>() {

			@Override
			protected Node createValue(String key) {
				return new Node(Country.this, key);
			}
		};
	}

	public Node getNode(String industry) {
		return nodes.obtain(industry);
	}

	public double getImports() {
		return nodes.values().stream().mapToDouble(Node::getImports).sum();
	}

	public double getExports() {
		return nodes.values().stream().mapToDouble(Node::getExports).sum();
	}

	public double getConsumption() {
		return nodes.values().stream().filter(n -> n.isConsumption()).mapToDouble(Node::getInputs).sum();
	}

	public double calculateComposition() {
		double diff = 0.0;
		for (Node n : nodes.values()) {
			diff = Math.max(diff, n.calculateComposition());
		}
		return diff;
	}

	public void updateComposition() {
		for (Node n : nodes.values()) {
			n.updateComposition();
		}
	}

	public Collection<Node> getNodes() {
		return nodes.values();
	}

	public void mergeConsumption() {
		Node consumption = new Node(this, Node.CONSUMPTION_TYPES[0]);
		assert nodes.get(Node.CONSUMPTION_TYPES[0]) == null;
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
		nodes.put(Node.CONSUMPTION_TYPES[0], consumption);
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
		ArrayList<Node> nodes = new ArrayList<>(this.nodes.values());
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
		for (Node n : nodes.values()) {
			if (n.getIndustry().equals(category)) {
				return n.getOrigin();
			}
		}
		return null;
	}

	public double getReusedImports() {
		return nodes.values().stream().mapToDouble(Node::getReusedImports).sum();
	}

	public void collapseRandomSectors(int seed, int sectors, BiConsumer<Node, Node> mergeListener) {
		Random rand = new Random(seed);
		ArrayList<Node> nodes = new ArrayList<>();
		for (Node n : this.nodes.values()) {
			if (!n.isConsumption()) {
				nodes.add(n);
			}
		}
		while (nodes.size() > sectors) {
			int level = nodes.size() - 1;
			Node n2 = nodes.remove(rand.nextInt(nodes.size()));
			Node n1 = nodes.remove(rand.nextInt(nodes.size()));
			if (mergeListener != null) {
				mergeListener.accept(n1, n2);
			}
			n1.absorb(n2, level);
			nodes.add(n1);
			this.nodes.remove(n2.getIndustry());
		}
	}

	public void collapseSmallestSectors(int sectors) {
		PriorityQueue<Node> nodes = new PriorityQueue<>(new Comparator<Node>() {

			@Override
			public int compare(Node o1, Node o2) {
				return Double.compare(o1.getOutputs(), o2.getOutputs());
			}
		});
		for (Node n : this.nodes.values()) {
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

}
