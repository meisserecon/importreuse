package com.meissereconomics.seminar;

public enum EFlowBendingMode {
	
	INPUTS, CONSUMPTION, BOTH;
	
	public static final EFlowBendingMode DEFAULT = EFlowBendingMode.INPUTS;

	public double calculate(double consumption, double input, double degree) {
		switch (this) {
		case INPUTS:
			return Math.min(consumption, input * degree);
		case CONSUMPTION:
			return Math.min(consumption * degree, input);
		default:
		case BOTH:
			return Math.min(consumption, input) * degree;
		}
	}

}
