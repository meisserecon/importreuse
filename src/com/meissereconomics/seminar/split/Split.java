package com.meissereconomics.seminar.split;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.util.DoubleArray;

import com.meissereconomics.seminar.Node;

public class Split {

	private double domesticConsumption;
	private double valueCreation;
	private double[] foreignConsumption;
	private double[] domesticInputs;
	private double[] foreignInputs;
	private double[] domesticOutputs;
	private double[] foreignOutputs;

	public Split(Node n1, Node n2) {
		assert n1.getCountry() == n2.getCountry();
		double v1 = n1.getCreatedValue();
		double v2 = n2.getCreatedValue();
		valueCreation = v1 / (v1 + v2);
		
		DoubleArray[] inputs = n1.getRelativeShares(n2, true);
		domesticInputs = inputs[0].getElements();
		assert inputs[1].getNumElements() == 0;
		foreignInputs = inputs[2].getElements();
		assert inputs[3].getNumElements() == 0;
		
		DoubleArray[] output = n1.getRelativeShares(n2, false);
		domesticOutputs = output[0].getElements();
		assert output[1].getNumElements() == 1;
		domesticConsumption = output[1].getElement(0);
		foreignOutputs = output[2].getElements();
		foreignConsumption = output[3].getElements();
	}
	
	private double mean(double[] domesticOutputs2) {
		return new Mean().evaluate(domesticOutputs2);
	}
	
	public static String getLabel(){
		return "Value Creation\tDomestic Consumption\tDomestic Inputs\tForeign Consumption\tForeign Inputs\tDomestic Outputs\tForeign Outputs";
	}
	
	@Override
	public String toString(){
		return valueCreation + "\t" + domesticConsumption + "\t" + mean(domesticInputs) + "\t" + mean(foreignConsumption) + "\t" + mean(foreignInputs) + "\t" + mean(domesticOutputs) + "\t" + mean(foreignOutputs);
	}

}
