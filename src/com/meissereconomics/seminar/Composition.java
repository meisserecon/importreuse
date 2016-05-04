package com.meissereconomics.seminar;

import java.util.Map;
import java.util.function.ObjDoubleConsumer;

import net.openhft.koloboke.collect.map.hash.HashObjDoubleMap;
import net.openhft.koloboke.collect.map.hash.HashObjDoubleMaps;
import net.openhft.koloboke.function.ObjDoubleToDoubleFunction;

public class Composition {

	private Country home;
	private boolean normalized;
	private double directConsumption;
	private HashObjDoubleMap<Country> shares;

	public Composition(Country country) {
		this.shares = HashObjDoubleMaps.newMutableMap();
		this.shares.put(country, 1.0);
		this.home = country;
		this.normalized = true;
		this.directConsumption = 0.0;
	}

	public Composition(Country country, double value) {
		this.home = country;
		this.shares = HashObjDoubleMaps.newMutableMap();
		assert!Double.isNaN(value);
		this.shares.put(country, value);
		this.normalized = false;
		this.directConsumption = 0.0;
	}

	public void include(Composition source, final double value) {
		assert value >= 0.0;
		assert source.normalized;
		assert!normalized;
		source.shares.forEach(new ObjDoubleConsumer<Country>() {

			@Override
			public void accept(Country t, double u) {
				double current = Composition.this.getShare(t);
				double newValue = current + u * value;
				assert!Double.isNaN(newValue);
				Composition.this.shares.put(t, newValue);
			}
		});
	}

	public void normalize() {
		final double[] sum = new double[1];
		this.shares.forEach(new ObjDoubleConsumer<Country>() {

			@Override
			public void accept(Country t, double u) {
				sum[0] += u;
			}
		});
		this.shares.replaceAll(new ObjDoubleToDoubleFunction<Country>() {

			@Override
			public double applyAsDouble(Country a, double b) {
				assert sum[0] > 0.0;
				return b / sum[0];
			}
		});
		this.normalized = true;
	}

	public double diff(Composition origin) {
		double difference = 0.0;
		for (Map.Entry<Country, Double> e : shares.entrySet()) {
			difference = Math.max(difference, Math.abs(e.getValue() - origin.getShare(e.getKey())));
		}
		return difference;
	}

	public double getImportReuse() {
		if (normalized) {
			return 1.0 - shares.getDouble(home);
		} else {
			return Double.NaN;
		}
	}

	public double getShare(Country key) {
		Double diff = Composition.this.shares.getDouble(key);
		assert!Double.isNaN(diff);
		return diff;
	}

	@Override
	public String toString() {
		return normalized ? getImportReuse() + "\timport reuse" : shares.toString();
	}

	public void redirectDomesticInputs(double consumption, EFlowBendingMode mode, double degree) {
		assert!normalized;
		double domestic = shares.getDouble(home);
		this.directConsumption = mode.calculate(consumption, domestic, degree);
		double share = domestic - directConsumption;
		if (share <= 0.0) {
			shares.removeAsDouble(home);
		} else {
			shares.put(home, share);
		}
	}

}
