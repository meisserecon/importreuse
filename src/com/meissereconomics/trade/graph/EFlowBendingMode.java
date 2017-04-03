package com.meissereconomics.trade.graph;

public enum EFlowBendingMode {

	HORIZONTAL_MIN, HORIZONTAL_MAX, HORIZONTAL_IN, HORIZONTAL_IN_IMP_AWARE, HORIZONTAL_OUT, HORIZONTAL_MID, VERTICAL_IN, VERTICAL_OUT, VERTICAL_MID, IMPORTS_AWARE_MIN, IMPORTS_AWARE_MAX, IMPORTS_AWARE_CON, IMPORTS_AWARE_IN, IMPORTS_AWARE_MID;

	public static final EFlowBendingMode DEFAULT = EFlowBendingMode.HORIZONTAL_IN;

	public double calculateDirectConsumption(double allDomestic, double input, double output, double consumption, double degree) {
		double valueCreation = consumption + output - input;
		switch (this) {
		case IMPORTS_AWARE_MIN:
			return Math.min(consumption, allDomestic) * degree;
		case IMPORTS_AWARE_MAX:
			return Math.min(consumption, Math.min(allDomestic, Math.max(consumption, allDomestic) * degree));
		case IMPORTS_AWARE_IN:
			return Math.min(consumption, allDomestic * degree);
		case IMPORTS_AWARE_CON:
			return Math.min(consumption * degree, allDomestic);
		case IMPORTS_AWARE_MID: {
			double mid = (consumption + allDomestic) / 2 * degree;
			return Math.min(mid, Math.min(consumption, allDomestic));
		}
		case HORIZONTAL_MIN: {
			double amount = Math.min(output, input) * degree;
			return translateToVertical(valueCreation, consumption, input, output, amount);
		}
		case HORIZONTAL_MAX: {
			double max = Math.max(output, input) * degree;
			double amount = Math.min(max, Math.min(output, input));
			return translateToVertical(valueCreation, consumption, input, output, amount);
		}
		case HORIZONTAL_IN: {
			double amount = Math.min(input * degree, output);
			return translateToVertical(valueCreation, consumption, input, output, amount);
		}
		case HORIZONTAL_IN_IMP_AWARE: {
			double imports = valueCreation + input - allDomestic;
			double passThroughTrade = Math.min(imports * degree, output);
			double normallyMixedOutputs = output - passThroughTrade;
			if (normallyMixedOutputs <= 0.0) {
				return allDomestic;
			} else {
				double temp = degree + (1 - degree) * normallyMixedOutputs / ((1 - degree) * imports + allDomestic);
				double newDomestic = output / temp - imports;
				return allDomestic - newDomestic;
			}
		}
		case HORIZONTAL_MID: {
			double mid = (input + output) / 2 * degree;
			double amount = Math.min(mid, Math.min(output, input));
			return translateToVertical(valueCreation, consumption, input, output, amount);
		}
		case HORIZONTAL_OUT: {
			double amount = Math.min(input, output * degree);
			return translateToVertical(valueCreation, consumption, input, output, amount);
		}
		case VERTICAL_IN:
			return Math.min(valueCreation * degree, consumption);
		default:
		case VERTICAL_MID:
			double mid = (valueCreation + consumption) / 2 * degree;
			return Math.min(mid, Math.min(consumption, valueCreation));
		case VERTICAL_OUT:
			return Math.min(valueCreation, consumption * degree);
		}

	}

	private double translateToVertical(double v, double c, double i, double o, double t) {
		double temp = v * c * t;
		if (temp == 0.0) {
			return 0.0;
		} else {
			return temp / (i * (o - t) + t * c);
		}
	}
}
