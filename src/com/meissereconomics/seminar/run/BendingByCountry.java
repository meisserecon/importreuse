package com.meissereconomics.seminar.run;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

import com.meissereconomics.seminar.Country;
import com.meissereconomics.seminar.EFlowBendingMode;
import com.meissereconomics.seminar.InputOutputGraph;
import com.meissereconomics.seminar.util.Formatter;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

import net.openhft.koloboke.collect.map.ObjDoubleMap;
import net.openhft.koloboke.collect.map.hash.HashObjDoubleMaps;

/**
 */
public class BendingByCountry {

	private static final double DEFAULT_BENDING = 0.4;
	private static final double EPSILON = 0.001;
	private static final EFlowBendingMode MODE = EFlowBendingMode.DEFAULT;

	private double[] levels;
	private InputOutputGraph[] graphs;
	private ObjDoubleMap<String> bendings;

	public BendingByCountry() throws FileNotFoundException, IOException {
		this.levels = new double[InputOutputGraph.SECTORS];
		this.graphs = new InputOutputGraph[InputOutputGraph.SECTORS];
		for (int i = 0; i < graphs.length; i++) {
			this.levels[i] = i + 1.0;
			this.graphs[i] = new InputOutputGraph("data/wiot05_row_apr12.CSV");
			this.graphs[i].collapseRandomSectors(i * 31, i + 1);
			this.graphs[i].deriveOrigins(MODE, DEFAULT_BENDING);
			System.out.println("Initialized level " + levels[i] + ", used " + Runtime.getRuntime().totalMemory() + " of " + Runtime.getRuntime().maxMemory());
		}
		this.bendings = HashObjDoubleMaps.newMutableMap();
		for (Country c : graphs[0].getCountries()) {
			this.bendings.put(c.getName(), DEFAULT_BENDING);
		}
		System.out.println("Initialization complete");
	}

	public void optimizeAll() {
		for (String country : bendings.keySet()) {
			optimizeCountry(country);
		}
	}

	public void printAll() {
		System.out.println("Country\tBending\tReuse\tVariance\tCovariance");
		ArrayList<String> countries = new ArrayList<>(bendings.keySet());
		java.util.Collections.sort(countries);
		for (String country : countries) {
			double bending = bendings.getDouble(country);
			double[] reuses = calculateReuse(country);
			double reuse = new Mean().evaluate(reuses);
			double variance = new Variance().evaluate(reuses);
			double covariance = new Covariance().covariance(levels, reuses);
			System.out.println(Formatter.toTabs(country, bending, reuse, variance, covariance));
		}
	}

	public void optimizeCountry(String country) {
		double bending1 = bendings.getDouble(country);
		double cov1 = calculateCovariance(country, bending1);
		if (Math.abs(cov1) > EPSILON) {
			double bending2 = findFirst(country, bending1, cov1 > 0);
			double bendingNew = bending2 <= 0.0 ? 0.0 : binarySearch(country, bending1, bending2);
			System.out.println("Updating bending of " + country + " from " + bending1 + " to " + bendingNew);
			this.bendings.put(country, bendingNew);
		}
	}

	private double binarySearch(String country, double bending1, double bending2) {
		if (bending1 > bending2) {
			return binarySearch(country, bending2, bending1);
		} else {
			double cov1 = calculateCovariance(country, bending1);
			if (cov1 <= 0.0) {
				return bending1;
			}
			double cov2 = calculateCovariance(country, bending2);
			assert cov2 < 0.0;
			while (Math.abs(bending2 - bending1) > EPSILON) {
				double middle = (bending1 + bending2) / 2;
				double covMiddle = calculateCovariance(country, middle);
				if (covMiddle < 0.0) {
					cov2 = covMiddle;
					bending2 = middle;
				} else {
					cov1 = covMiddle;
					bending1 = middle;
				}
			}
			return (bending1 + bending2) / 2;
		}
	}

	private double findFirst(String country, double bending, boolean upwards) {
		while (true) {
			if (upwards) {
				bending += 0.05;
			} else {
				bending -= 0.05;
			}
			double cov = calculateCovariance(country, bending);
			if ((cov <= 0.0 && upwards) || (cov >= 0.0 && !upwards)) {
				return bending;
			} else if (bending <= 0.0) {
				return 0.0;
			}
		}
	}

	protected double calculateCovariance(String country, double bending) {
		for (InputOutputGraph graph : graphs) {
			graph.getCountry(country).deriveOrigins(MODE, bending);
		}
		double cov = new Covariance().covariance(levels, calculateReuse(country));
		return cov;
	}

	protected double[] calculateReuse(String country) {
		double[] reuse = new double[levels.length];
		for (int i = 0; i < reuse.length; i++) {
			reuse[i] = graphs[i].getCountry(country).getImportReuse();
		}
		return reuse;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		BendingByCountry bendings = new BendingByCountry();
		bendings.optimizeAll();
		bendings.printAll();
	}

}
