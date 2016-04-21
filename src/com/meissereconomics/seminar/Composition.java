package com.meissereconomics.seminar;

import java.util.Map;
import java.util.function.ObjDoubleConsumer;

import net.openhft.koloboke.collect.map.hash.HashObjDoubleMap;
import net.openhft.koloboke.collect.map.hash.HashObjDoubleMaps;
import net.openhft.koloboke.function.ObjDoubleToDoubleFunction;

public class Composition {

	private Country home;
	private boolean normalized;
	private HashObjDoubleMap<Country> shares;

	public Composition(Country country) {
		this.shares = HashObjDoubleMaps.newMutableMap();
		this.shares.put(country, 1.0);
		this.home = country;
		this.normalized = true;
	}

	public Composition(Country country, double value) {
		this.home = country;
		this.shares = HashObjDoubleMaps.newMutableMap();
		this.shares.put(country, value);
		this.normalized = false;
	}

	public void include(Composition source, double value) {
		assert value >= 0.0;
		assert source.normalized;
		assert!normalized;
		source.shares.forEach(new ObjDoubleConsumer<Country>() {

			@Override
			public void accept(Country t, double u) {
				double current = Composition.this.getShare(t);
				Composition.this.shares.put(t, current + u * value);
			}
		});
	}

	public void normalize() {
		double[] sum = new double[1];
		this.shares.forEach(new ObjDoubleConsumer<Country>() {

			@Override
			public void accept(Country t, double u) {
				sum[0] += u;
			}
		});
		this.shares.replaceAll(new ObjDoubleToDoubleFunction<Country>() {

			@Override
			public double applyAsDouble(Country a, double b) {
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
		return 1.0 - shares.getDouble(home);
	}

	public double getShare(Country key) {
		Double diff = Composition.this.shares.getDouble(key);
		assert!Double.isNaN(diff);
		return diff;
	}

	@Override
	public String toString() {
		return getImportReuse() + "\timport reuse";
	}

}
