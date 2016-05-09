package com.meissereconomics.seminar;

public class Composition {

	private int home;
	private double directConsumption;
	private double[] shares;
	private boolean normalized;

	public Composition(int countries, int home) {
		this.shares = new double[countries];
		this.shares[home] = 1.0;
		this.home = home;
		this.directConsumption = 0.0;
		this.normalized = true;
	}

	public Composition(int countries, int home, double value) {
		this.home = home;
		this.shares = new double[countries];
		this.shares[home] =  value;
		this.directConsumption = 0.0;
		this.normalized = false;
	}

	private boolean isNormalized() {
		return normalized;
	}

	public void include(Composition source, final double value) {
		assert !normalized;
		assert source.normalized;
		assert value >= 0.0;
		for (int i=0; i<shares.length; i++){
			double sourceShare = source.shares[i];
			this.shares[i] = this.shares[i] + sourceShare * value;
		}
	}

	public void normalize() {
		assert !normalized;
		double sum = 0.0;
		for (double s: shares){
			sum += s;
		}
		for (int i=0; i<shares.length; i++){
			this.shares[i] /= sum;
		}
		this.normalized = true;
	}

	public double diff(Composition origin) {
		double difference = 0.0;
		for (int i=0; i<shares.length; i++){
			difference = Math.max(difference, Math.abs(shares[i] - origin.shares[i]));
		}
		return difference;
	}

	public double getImportReuse() {
		if (isNormalized()) {
			return 1.0 - shares[home];
		} else {
			return Float.NaN;
		}
	}

	public void redirectDomesticInputs(double consumption, EFlowBendingMode mode, double degree) {
		assert !isNormalized();
		double domestic = shares[home];
		this.directConsumption = mode.calculate(consumption, domestic, degree);
		double share = domestic - directConsumption;
		this.shares[home] = Math.max(0.0, share);
	}

	public int getCountryCount() {
		return shares.length;
	}

	@Override
	public String toString() {
		return isNormalized() ? getImportReuse() + "\timport reuse" : shares.toString();
	}

}
