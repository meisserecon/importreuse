package com.meissereconomics.seminar;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ToDoubleFunction;

import org.apache.commons.math3.util.DoubleArray;
import org.apache.commons.math3.util.ResizableDoubleArray;

import com.meissereconomics.seminar.util.Formatter;

import net.openhft.koloboke.collect.map.hash.HashObjDoubleMap;
import net.openhft.koloboke.collect.map.hash.HashObjDoubleMaps;

public class Node {

	public final static String[] CONSUMPTION_TYPES = {
			// WIOD
			"Consumption and capital formation", "Final consumption expenditure by households", "Final consumption expenditure by non-profit organisations serving households (NPISH)",
			"Final consumption expenditure by government", "Gross fixed capital formation", "Changes in inventories and valuables",
			// OECD
			"CONS: Consumption expenditure", "GFCF: Gross Fixed Capital Formation", "INVNT: Changes in inventories", "CONS_ABR: Direct purchases abroad by residents (imports)",
			"CONS_NONRES: Direct purchases by non-residents (exports)" };

	{
		for (int i = 0; i < CONSUMPTION_TYPES.length; i++) {
			CONSUMPTION_TYPES[i] = CONSUMPTION_TYPES[i].intern();
		}
	}

	private final Country country;
	private final String industry;
	private Composition origin;
	private HashObjDoubleMap<Node> outputs, inputs;

	public Node(Country c, String industry) {
		this.country = c;
		this.industry = industry;
		this.origin = new Composition(c.getCountries(), country.getNumber());
		this.inputs = HashObjDoubleMaps.newMutableMap();
		this.outputs = HashObjDoubleMaps.newMutableMap();
	}
	
	public void forEachOutput(ObjDoubleConsumer<Node> consumer){
		this.outputs.forEach(consumer);
	}

	public void forEachLocalInputs(ObjDoubleConsumer<Node> consumer) {
		this.inputs.forEach(new ObjDoubleConsumer<Node>() {

			@Override
			public void accept(Node t, double value) {
				if (t.country == Node.this.country) {
					consumer.accept(t, value);
				}
			}
		});
	}

	public double getCreatedValue() {
		if (isConsumption()) {
			return 0.0;
		} else {
			return getOutputs() - getInputs();
		}
	}

	public double calculateComposition(Node consumption, EFlowBendingMode mode, double extent) {
		double input = getInputs();
		double cons = consumption == this ? input : outputs.getDouble(consumption);
		double output = consumption == this ? 0.0 : getOutputs() - cons;

		Composition comp = new Composition(origin.getCountryCount(), country.getNumber(), input, output, cons);
		inputs.forEach(new ObjDoubleConsumer<Node>() {

			@Override
			public void accept(Node t, double value) {
				comp.include(t.getOrigin(), value);
			}
		});
		if (!isConsumption()) {
			comp.redirectDomesticInputs(mode, extent);
		}
		comp.normalize();
		double difference = comp.diff(origin);
		this.origin = comp;
		return difference;
	}

	protected double shortWireConsumption(Node consumption, double localConsumptionPreference) {
		double value = Math.max(0, getCreatedValue());
		double localConsumption = outputs.getDouble(consumption);
		double preferentialFlow = localConsumptionPreference * Math.min(localConsumption, value);
		value -= preferentialFlow;
		return value;
	}

	public Composition getOrigin() {
		return origin;
	}

	public double getIntensity(boolean imports) {
		final double[] sum = new double[2];
		(imports ? inputs : outputs).forEach(new ObjDoubleConsumer<Node>() {

			@Override
			public void accept(Node t, double value) {
				if (t.getCountry() != country) {
					sum[0] += value;
				}
				sum[1] += value;
			}
		});
		return sum[1] == 0.0 ? 0.0 : sum[0] / sum[1];
	}

	public void absorb(final Node other, final int reporting) {
		assert other != this;
		double valueAdded = getCreatedValue();
		double otherValue = other.getCreatedValue();
		absorbSelfReferences(other);
		other.outputs.forEach(new ObjDoubleConsumer<Node>() {

			@Override
			public void accept(Node othersDestination, double value) {
				assert other != othersDestination;
				assert other != Node.this;
				double existing = othersDestination.inputs.containsKey(Node.this) ? othersDestination.inputs.getDouble(Node.this) : 0.0;
				assert existing == (Node.this.outputs.containsKey(othersDestination) ? Node.this.outputs.getDouble(othersDestination) : 0.0);
				double additional = othersDestination.inputs.removeAsDouble(other);
				othersDestination.inputs.shrink();
				assert additional == value;
				double sum = existing + additional;
				othersDestination.inputs.put(Node.this, sum);
				Node.this.outputs.put(othersDestination, sum);
			}
		});
		other.inputs.forEach(new ObjDoubleConsumer<Node>() {

			@Override
			public void accept(Node othersSource, double value) {
				assert other != othersSource;
				assert other != Node.this;
				double existing = othersSource.outputs.containsKey(Node.this) ? othersSource.outputs.getDouble(Node.this) : 0.0;
				assert existing == (Node.this.inputs.containsKey(othersSource) ? Node.this.inputs.getDouble(othersSource) : 0.0);
				double additional = othersSource.outputs.removeAsDouble(other);
				othersSource.outputs.shrink();
				assert additional == value;
				double sum = existing + additional;
				othersSource.outputs.put(Node.this, sum);
				Node.this.inputs.put(othersSource, sum);
			}
		});
		assert Math.abs(getCreatedValue() - (otherValue + valueAdded)) < 0.01;
	}

	protected void absorbSelfReferences(final Node other) {
		double self = inputs.getDouble(this);
		assert self == outputs.getDouble(this);
		double incoming = other.outputs.removeAsDouble(this);
		double verification = inputs.removeAsDouble(other);
		assert incoming == verification;
		double outgoing = other.inputs.removeAsDouble(this);
		double verification2 = outputs.removeAsDouble(other);
		assert outgoing == verification2;
		double otherSelf = other.inputs.removeAsDouble(other);
		double verification3 = other.outputs.removeAsDouble(other);
		assert otherSelf == verification3;
		double sum = self + incoming + outgoing + otherSelf;
		inputs.put(this, sum);
		outputs.put(this, sum);
		inputs.shrink();
		outputs.shrink();
	}

	public Country getCountry() {
		return country;
	}
	
	public void linkOrAdd(Node dest, double value) {
		if (outputs.containsKey(dest)){
			updateLink(dest, outputs.getDouble(dest) + value);
		} else {
			linkTo(dest, value);
		}
	}

	public void linkTo(Node node, double millions) {
		if (millions != 0.0) {
			assert !Double.isNaN(millions);
			assert !this.outputs.containsKey(node);
			this.outputs.put(node, millions);
			assert !node.inputs.containsKey(this);
			node.inputs.put(this, millions);
		}
	}

	public void updateLink(Node node, double millions) {
		assert this.outputs.containsKey(node);
		assert node.inputs.containsKey(this);
		this.outputs.put(node, millions);
		node.inputs.put(this, millions);
	}

	public void turnNegativeInputs() {
		double value = getCreatedValue();
		Iterator<Map.Entry<Node, Double>> iter = inputs.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<Node, Double> next = iter.next();
			if (next.getValue() < 0.0) {
				Node dest = next.getKey();
				assert dest != this; // not sure if it would work
				if (!isConsumption()) { // ignore negative consumption
					linkOrAdd(dest, -next.getValue());
				}
				next.getKey().outputs.removeAsDouble(this);
				iter.remove();
			}
		}
		assert isConsumption() || Math.abs(value - getCreatedValue()) < 0.01;
	}

	public double getOutputs() {
		return outputs.values().stream().mapToDouble(n -> n.doubleValue()).sum();
	}

	public double getInputs() {
		return inputs.values().stream().mapToDouble(n -> n.doubleValue()).sum();
	}

	public double getExports() {
		return reduce(outputs);
	}

	public double getReusedImports() {
		return outputs.entrySet().stream().filter(n -> n.getKey().getCountry() != country).mapToDouble(new ToDoubleFunction<Map.Entry<Node, Double>>() {

			@Override
			public double applyAsDouble(Entry<Node, Double> value) {
				double v = value.getValue().doubleValue() * origin.getImportReuse();
				assert !Double.isNaN(v);
				return v;
			}
		}).sum();
	}

	public double getImports() {
		return reduce(inputs);
	}

	private double reduce(HashObjDoubleMap<Node> outputs) {
		final double[] sum = new double[1];
		outputs.forEach(new ObjDoubleConsumer<Node>() {

			@Override
			public void accept(Node t, double value) {
				if (t.getCountry() != country) {
					sum[0] += value;
				}
			}
		});
		return sum[0];
	}

	public String getStats() {
		return industry + " generates " + getCreatedValue() + " with inputs " + getInputs() + " and outputs " + getOutputs() + ", while importing " + getImports() + " and exporting " + getExports();
	}

	public boolean isConsumption() {
		for (String s : CONSUMPTION_TYPES) {
			if (industry == s) {
				return true;
			}
		}
		return false;
	}

	public String getIndustry() {
		return industry;
	}

	@Override
	public String toString() {
		String s1 = Formatter.toTabs(country, industry, getInputs(), getOutputs(), getDomesticConsumption());
//		s1 += "\tINPUTS:";
//		for (Map.Entry<Node, Double> e : inputs.entrySet()) {
//			if (e.getKey().country == country) {
//				s1 += "\t" + e.getKey().industry + "\t" + e.getValue();
//			}
//		}
		// s1 += "\tOUTPUTS:";
		// for (Map.Entry<Node, Double> e : outputs.entrySet()) {
		// s1 += "\t" + e.getKey() + "\t" + e.getValue();
		// }
		return s1;
	}

	public String getDescription() {
		return country + " " + industry + " importing " + getImports() + " and exporting " + getExports() + " out of which " + getReusedImports() + " is reused";
	}

	public double getDomesticConsumption() {
		for (Node n : outputs.keySet()) {
			if (n.isConsumption() && n.getCountry() == country) {
				return outputs.getDouble(n);
			}
		}
		return 0.0;
	}

	/**
	 * Returns the relative input or output shares for all connections to other nodes, excluding self-connections and connections between this and other.
	 * 
	 */
	public DoubleArray[] getRelativeShares(final Node other, final boolean input) {
		assert !isConsumption();
		assert !other.isConsumption();
		assert other.country == country;
		final DoubleArray domestic = new ResizableDoubleArray();
		final DoubleArray foreign = new ResizableDoubleArray();
		final DoubleArray domesticConsumption = new ResizableDoubleArray();
		final DoubleArray foreignConsumption = new ResizableDoubleArray();
		HashObjDoubleMap<Node> myMap = (input ? inputs : outputs);
		myMap.forEach(new ObjDoubleConsumer<Node>() {

			@Override
			public void accept(Node t, double value) {
				if (t != Node.this && t != other) {
					double othersValue = (input ? other.inputs : other.outputs).getDouble(t);
					double share = value / (value + othersValue);
					getTarget(t).addElement(Math.min(1.0, share));
				}
			}

			private DoubleArray getTarget(Node t) {
				boolean sameCountry = t.country == country;
				boolean consumption = t.isConsumption();
				assert !(input && consumption);
				if (sameCountry) {
					return consumption ? domesticConsumption : domestic;
				} else {
					return consumption ? foreignConsumption : foreign;
				}
			}

		});
		// all the sectors we are not connected to but the other node is
		for (Node t : (input ? other.inputs : other.outputs).keySet()) {
			if (!myMap.containsKey(t) && t != other && t != this) {
				boolean sameCountry = t.country == country;
				if (sameCountry) {
					(t.isConsumption() ? domesticConsumption : domestic).addElement(0.0);
				} else {
					(t.isConsumption() ? foreignConsumption : foreign).addElement(0.0);
				}
			}
		}
		return new DoubleArray[] { domestic, domesticConsumption, foreign, foreignConsumption };
	}

	@Override
	public int hashCode() {
		return country.hashCode() ^ industry.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		Node ok = (Node) o;
		return country == ok.country && industry.equals(ok.industry);
	}

	public double getOutput(Node n1) {
		return outputs.getDouble(n1);
	}

}
