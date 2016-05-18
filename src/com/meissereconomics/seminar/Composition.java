package com.meissereconomics.seminar;

public class Composition {

	private int home;
	private double[] shares;
	private boolean normalized;
	private double input, output, consumption;

	public Composition(int countries, int home) {
		this.shares = new double[countries];
		this.shares[home] = 1.0;
		this.home = home;
		this.normalized = true;
	}

	public Composition(int countries, int home, double input, double output, double consumption) {
		this.home = home;
		this.input = input;
		this.output = output;
		this.consumption = consumption;
		this.shares = new double[countries];
		this.shares[home] = output + consumption - input;
		assert !Double.isNaN(this.shares[home]);
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
		if (sum == 0.0){
			sum = 1.0;
			this.shares[home] = 1.0;
		}
		assert !Double.isNaN(sum);
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

	public void redirectDomesticInputs(EFlowBendingMode mode, double degree) {
		assert !isNormalized();
		this.shares[home] -= mode.calculateDirectConsumption(shares[home], input, output, consumption, degree);
		assert !Double.isNaN(this.shares[home]);
		if (this.shares[home] < 0.0){
			this.shares[home] = 0.0; // fix rounding errors.
		}
	}

	public int getCountryCount() {
		return shares.length;
	}

	@Override
	public String toString() {
		return isNormalized() ? getImportReuse() + "\timport reuse" : shares.toString();
	}

}
