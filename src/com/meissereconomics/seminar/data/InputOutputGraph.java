package com.meissereconomics.seminar.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.ObjDoubleConsumer;

import com.meissereconomics.seminar.Country;
import com.meissereconomics.seminar.EFlowBendingMode;
import com.meissereconomics.seminar.Node;
import com.meissereconomics.seminar.util.InstantiatingHashmap;

public class InputOutputGraph {
	
	private final InstantiatingHashmap<String, Country> countries;

	public InputOutputGraph(final int countries) {
		this.countries = new InstantiatingHashmap<String, Country>() {

			private int count = 0;

			@Override
			protected Country createValue(String name) {
				return new Country(name, count++, countries);
			}
		};
	}

	public void collapseSmallestSectors(int i) {
		for (Country c : countries.values()) {
			c.collapseSmallestSectors(i);
		}
	}

	public void collapseRandomSectors(int seed, int i) {
		collapseRandomSectors(seed, i, null);
	}

	public void collapseRandomSectors(int seed, int i, String except) {
		for (Country c : countries.values()) {
			if (!c.getName().equals(except)) {
				seed += 131313;
				c.collapseRandomSectors(seed, i);
			}
		}
	}

	public void deriveOrigins(EFlowBendingMode mode, double consumptionPreference) {
		double difference = 1.0;
		while (difference >= 0.005) {
			difference = 0.0;
			for (Country c : countries.values()) {
				difference = Math.max(c.calculateComposition(mode, consumptionPreference), difference);
			}
		}
	}

	public double getGlobalImportReuse() {
		double totalReuse = 0.0;
		double totalExports = 0.0;
		for (Country c : countries.values()) {
			if (!c.isRestOfTheWorld()){
				totalExports += c.getExports();
				totalReuse += c.getReusedImports();
			}
		}
		return totalReuse / totalExports;
	}

	private void printImportExportStats() {
		System.out.println("Country\tImports\tExports\tImport Reuse\tConsumption");
		for (Country c : countries.values()) {
			double imports = c.getImports();
			double exports = c.getExports();
			double importReuse = c.getReusedImports();
			double size = c.getConsumption();
			System.out.println(c + "\t" + imports + "\t" + exports + "\t" + importReuse + "\t" + size);
		}
		// printReuse("Machinery, Nec");
		// printReuse("Coke, Refined Petroleum and Nuclear Fuel");
		// printReuse(Node.CONSUMPTION_TYPES[0]);
	}

	protected void printReuse(String industry) {
		System.out.println(industry);
		for (Country c : getCountries()) {
			System.out.println(c + "\t" + c.getOriginOf(industry));
		}
	}

	public Country getCountry(String string) {
		return countries.obtain(string);
	}

	public Collection<Country> getCountries() {
		ArrayList<Country> cs = new ArrayList<>(countries.values());
		Collections.sort(cs);
		return cs;
	}

	public int getSectors() {
		for (Country c: countries.values()){
			if (!c.isRestOfTheWorld()){
				return c.getSectors();
			}
		}
		return 0;
	}
	
	public InputOutputGraph copy(){
		InputOutputGraph copy = new InputOutputGraph(countries.size());
		for (final Country c: countries.values()){
			for (final Node n: c.getNodeList()){
				n.forEachOutput(new ObjDoubleConsumer<Node>() {
					
					@Override
					public void accept(Node t, double value) {
						Node source = copy.getCountry(c.getName()).getNode(n.getIndustry());
						Node target = copy.getCountry(t.getCountry().getName()).getNode(t.getIndustry());
						source.linkTo(target, value);
					}
				});
			}
		}
		return copy;
	}

}