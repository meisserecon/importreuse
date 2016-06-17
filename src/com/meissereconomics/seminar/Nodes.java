package com.meissereconomics.seminar;

import java.util.ArrayList;
import java.util.Collection;

import com.meissereconomics.seminar.util.InstantiatingHashmap;

public class Nodes {
	
	private int modcount;
	private InstantiatingHashmap<String, Node> nodes;
	private ArrayList<Node> nodeList;
	
	public Nodes(final Country country){
		this.modcount = 1;
		this.nodes = new InstantiatingHashmap<String, Node>() {

			@Override
			protected Node createValue(String key) {
				touch();
				return new Node(country, key);
			}
		};
	}

	public int getModCount() {
		return modcount;
	}

	protected void touch() {
		this.nodeList = null;
		this.modcount++;
	}

	public ArrayList<Node> getList() {
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

	public Node get(String industry) {
		return nodes.obtain(industry);
	}

	public boolean contains(String industry) {
		return nodes.containsKey(industry);
	}

	public Collection<Node> values() {
		return nodes.values();
	}

	public void put(String string, Node consumption) {
		this.nodes.put(string, consumption);
		this.touch();
	}

	public int size() {
		return nodes.size();
	}

	public void remove(String industry) {
		this.nodes.remove(industry);
		touch();
	}

	public int getNonConsumptionSectors() {
		int count = 0;
		for (Node n: nodes.values()){
			if (!n.isConsumption()){
				count++;
			}
		}
		return count;
	}

}
