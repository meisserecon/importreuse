package com.meissereconomics.seminar;

import java.util.ArrayList;

import org.junit.Test;

public class NodeTest {

	@Test
	public void testMerge() {
		Country c1 = createCountry();
		c1.collapseRandomSectors(1, 1, null);
		double reuse1 = c1.getReusedImports();
		for (int i = 0; i < 10; i++) {
			Country c2 = createCountry();
			c2.collapseRandomSectors(1, 1, null);
			double reuse2 = c2.getReusedImports();
			assert reuse1 == reuse2;
		}
	}

	private Country createCountry() {
		Country c1 = new Country("Aland");
		Node con1 = c1.getNode(Node.CONSUMPTION_TYPES[0]);
		Country c2 = new Country("Bland");
		Node con2 = c2.getNode(Node.CONSUMPTION_TYPES[0]);

		ArrayList<Node> sources = new ArrayList<>();
		sources.add(c1.getNode("Cars"));
		sources.add(c2.getNode("Cars"));
		sources.add(c1.getNode("Computers"));
		sources.add(c2.getNode("Computers"));
		ArrayList<Node> all = new ArrayList<>(sources);
		sources.add(con1);
		sources.add(con2);
		int millions = 10;
		for (Node source : sources) {
			for (Node dest : all) {
				source.linkTo(dest, millions++);
			}
		}
		return c1;
	}

}
