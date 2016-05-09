package com.meissereconomics.seminar;

import org.junit.Test;

public class RedirectionTest {
	
	@Test
	public void testRedirection(){
		Country other = new Country("Other", 0, 2);
		Country home = new Country("Home", 1, 2);
		Node source = other.getNode("Industry");
		Node stage1 = home.getNode("Stage 1");
		source.linkTo(stage1, 100);
		stage1.linkTo(home.getConsumptionNode(), 50);
		Node stage2 = home.getNode("Stage 2");
		stage1.linkTo(stage2, 200);
		stage2.linkTo(home.getConsumptionNode(), 50);
		Node stage3 = home.getNode("Stage 3");
		stage2.linkTo(stage3, 150);
		stage3.linkTo(home.getConsumptionNode(), 50);
		stage3.linkTo(other.getConsumptionNode(), 100);
		double diff = 1.0;
		while (diff >= 0.001){
			diff = home.calculateComposition(EFlowBendingMode.BOTH, 1.0);
		}
		double reuse = home.getReusedImports() / home.getExports();
		assert reuse >= 0.999;
		
	}

}
