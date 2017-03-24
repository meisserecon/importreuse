package com.meissereconomics.trade.run;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.meissereconomics.trade.Country;
import com.meissereconomics.trade.EFlowBendingMode;
import com.meissereconomics.trade.Node;
import com.meissereconomics.trade.data.InputOutputGraph;
import com.meissereconomics.trade.data.USGraph;
import com.meissereconomics.trade.util.Formatter;

public class GraphComparison {
	
	public GraphComparison(InputOutputGraph g1, InputOutputGraph g2){
		printInfo(g1);
		printInfo(g2);
		manualMerge(g2.getCountry("USA"));
		printInfo(g2);
		
		Country c1 = g1.getCountry("USA");
		Country c2 = g2.getCountry("USA");
		for (Node n: c1.getNodeList()){
			if (!c2.hasNode(n.getIndustry())){
				System.out.println(n.getIndustry() + " missing");
			}
		}
		
		System.out.println("asdasdasdasd");
		for (Node n: c2.getNodeList()){
			if (!c1.hasNode(n.getIndustry())){
				System.out.println(n.getIndustry() + " missing");
			}
		}
	}
	
	private void printInfo(InputOutputGraph g1) {
		g1.deriveOrigins(EFlowBendingMode.DEFAULT, 0.0);
		Country c = g1.getCountry("USA");
		System.out.println(Formatter.toTabs(new double[]{c.getNodeList().size(), c.getConsumption(), c.getCreatedValue(), c.getImports(), c.getExports(), c.getImportReuse()}));
	}

	private void manualMerge(Country usa) {
		merge(usa, "22");
		merge(usa, "23");
		merge(usa, "42");
		merge(usa, "55");
		merge(usa, "61");
		merge(usa, "81");
		usa.merge("111", "112");
		usa.merge("113", "114");
		usa.merge("113", "115");
		usa.merge("230", "233");
		usa.merge("311", "312");
		usa.merge("313", "314");
		usa.merge("315", "316");
		usa.merge("492", "48A");
		usa.merge("515", "517");
		usa.merge("518", "519");
		usa.merge("522", "52A");
		usa.merge("532", "533");
		usa.merge("711", "712");
		usa.merge("3361", "3362");
		usa.merge("3361", "3363");
		usa.merge("3364", "3365");
		usa.merge("3364", "3366");
		usa.merge("3364", "3369");
		usa.merge("5412", "5413");
		usa.merge("5412", "5414");
		usa.merge("5412", "5416");
		usa.merge("5412", "5417");
		usa.merge("5412", "5418");
		usa.merge("5412", "5419");
		usa.merge("S001", "491");
		usa.merge("S009", "S003");
	}

	private void merge(Country usa, String prefix) {
		Node n1 = null;
		for (Node n: usa.getNodeList()){
			if (n.getIndustry().startsWith(prefix)){
				if (n1 == null){
					n1 = n;
				} else {
					usa.merge(n1.getIndustry(), n.getIndustry());
				}
			}
		}
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		new GraphComparison(new USGraph(false), new USGraph(true));
	}

}
