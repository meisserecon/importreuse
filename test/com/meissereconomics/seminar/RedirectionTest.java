package com.meissereconomics.seminar;

import org.junit.Test;

import com.meissereconomics.trade.Country;
import com.meissereconomics.trade.EFlowBendingMode;
import com.meissereconomics.trade.Node;

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
		double reuse = calcReuse(home, EFlowBendingMode.VERTICAL_IN, 1.0);
		for (EFlowBendingMode m: EFlowBendingMode.values()){
			assert reuse == calcReuse(home, m, 1.0);
		}
		reuse = calcReuse(home, EFlowBendingMode.VERTICAL_IN, 0.0);
		for (EFlowBendingMode m: EFlowBendingMode.values()){
			assert reuse == calcReuse(home, m, 0.0);
		}
	}

	protected double calcReuse(Country home, EFlowBendingMode mode, double extent) {
		double diff = 1.0;
		double epsilon = 0.0005;
		while (diff >= epsilon){
			diff = home.calculateComposition(mode, extent, diff * epsilon);
		}
		double reuse = home.getReusedImports() / home.getExports();
		return reuse;
	}

}
