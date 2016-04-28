package com.meissereconomics.seminar;

import java.util.ArrayList;

import org.junit.Test;

public class NodeTest {

	private static final String CON = Node.CONSUMPTION_TYPES[0];
	private static final String CAR = "Car";
	private static final String COM = "Computers";
	private static final String WAT = "Water";

	private static final double ACCURACY = 0.000001;

	@Test
	public void testMerge() {
		Country[] cs = createCountries();
		calculateComposition(cs, 3);
		double reuse1 = cs[0].getReusedImports();
		calculateComposition(cs, 1);
		reuse1 = cs[0].getReusedImports();
		for (int i = 0; i < 10; i++) {
			Country[] c2 = createCountries();
			calculateComposition(c2, 1);
			double reuse2 = c2[0].getReusedImports();
			assert Math.abs(reuse1 - reuse2) / reuse2 < ACCURACY * 10;
		}
	}

	@Test
	public void testFlow() {
		Country c = createCountries()[0];
		double maxDomesticConsumption = c.getMaxDomesticFlow(false);
		double maxImportReuse = c.getMaxDomesticFlow(true);
		System.out.println(maxDomesticConsumption);
		System.out.println(maxImportReuse);
		assert equals(c.getImports() + c.getCreatedValue(), c.getExports() + c.getConsumption());
		assert equals(c.getExports() - (c.getCreatedValue() - maxDomesticConsumption), maxImportReuse);
		c.deriveOrigins(EFlowBendingMode.BOTH, 1.0);
		assert equals(maxImportReuse, c.getReusedImports());
	}

	private boolean equals(double d, double e) {
		return Math.abs(d - e) < 0.001;
	}

	private void calculateComposition(Country[] cs, int sectors) {
		for (Country c : cs) {
			c.collapseRandomSectors(13, sectors);
		}
		double diff = 1.0;
		while (diff > ACCURACY) {
			for (Country c : cs) {
				diff = c.calculateComposition(EFlowBendingMode.BOTH, 0.0);
				c.updateComposition();
			}
		}
	}

	private Country[] createCountries() {
		Country c1 = new Country("Aland");
		Node con1 = c1.getNode(CON);
		Country c2 = new Country("Bland");
		Node con2 = c2.getNode(CON);

		ArrayList<Node> sources = new ArrayList<>();
		sources.add(c1.getNode(CAR));
		sources.add(c2.getNode(CAR));
		sources.add(c1.getNode(COM));
		sources.add(c2.getNode(COM));
		sources.add(c1.getNode(WAT));
		sources.add(c2.getNode(WAT));
		ArrayList<Node> all = new ArrayList<>(sources);
		all.add(con1);
		all.add(con2);
		for (Node source : sources) {
			for (Node dest : all) {
				source.linkTo(dest, 3);
			}
		}
		c1.getNode(WAT).updateLink(c1.getNode(CON), 100);
		c2.getNode(WAT).updateLink(c2.getNode(CON), 100);
		c1.getNode(COM).updateLink(c1.getNode(CAR), 100);
		c2.getNode(COM).updateLink(c1.getNode(CAR), 100);
		c1.getNode(COM).updateLink(c2.getNode(CAR), 100);
		c2.getNode(COM).updateLink(c2.getNode(CAR), 100);
		c1.getNode(CAR).updateLink(c1.getNode(CON), 200);
		c2.getNode(CAR).updateLink(c1.getNode(CON), 200);
		c1.getNode(CAR).updateLink(c2.getNode(CON), 200);
		c2.getNode(CAR).updateLink(c2.getNode(CON), 200);
		assert c1.getImports() == c2.getExports();
		assert c1.getImports() == c2.getImports();
		assert c1.getImports() == c1.getExports();
		return new Country[] { c1, c2 };
	}

}
