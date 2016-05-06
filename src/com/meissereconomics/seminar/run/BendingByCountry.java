package com.meissereconomics.seminar.run;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.math3.stat.correlation.Covariance;

import com.meissereconomics.seminar.Country;
import com.meissereconomics.seminar.EFlowBendingMode;
import com.meissereconomics.seminar.InputOutputGraph;

import net.openhft.koloboke.collect.map.ObjDoubleMap;
import net.openhft.koloboke.collect.map.hash.HashObjDoubleMaps;

/**
 */
public class BendingByCountry {

	private static final double DEFAULT_BENDING = 0.0;
	private static final double EPSILON = 0.001;
	private static final EFlowBendingMode MODE = EFlowBendingMode.DEFAULT;

	private int year;
	private double[] levels;
	private InputOutputGraph[] graphs;
	private ObjDoubleMap<String> bendings;
	
	public BendingByCountry(int year) throws FileNotFoundException, IOException {
		this.year = year;
		String file = getFilename(year);
//		System.out.println("Processing file " + file);
		this.levels = new double[InputOutputGraph.SECTORS];
		this.graphs = new InputOutputGraph[1]; // TEMP InputOutputGraph.SECTORS];
		for (int i = 0; i < graphs.length; i++) {
			this.levels[i] = i + 1.0;
			this.graphs[i] = new InputOutputGraph(file);
//			this.graphs[i].collapseRandomSectors(i * 31, i + 1);
			this.graphs[i].deriveOrigins(MODE, DEFAULT_BENDING);
//			System.out.println("Initialized level " + levels[i] + ", used " + Runtime.getRuntime().totalMemory() + " of " + Runtime.getRuntime().maxMemory());
		}
		this.bendings = HashObjDoubleMaps.newMutableMap();
		for (Country c : graphs[0].getCountries()) {
			this.bendings.put(c.getName(), DEFAULT_BENDING);
		}
//		System.out.println("Initialization complete");
	}
	
	private static String getFilename(int year){
		if (year <= 1999){
			return "data/wiot" + (year - 1900) + "_row_apr12.csv";
		} else if (year <= 2007){
			return "data/wiot0" + (year - 2000) + "_row_apr12.csv";
		} else if (year <= 2009){
			return "data/wiot0" + (year - 2000) + "_row_sep12.csv";
		} else {
			return "data/wiot" + (year - 2000) + "_row_sep12.csv";
		}
	}

	public void optimizeAll() {
		for (String country : bendings.keySet()) {
			optimizeCountry(country);
		}
	}

	public void printAll() {
//		System.out.println("Country\tBending\tReuse\tVariance\tCovariance");
		ArrayList<String> countries = new ArrayList<>(bendings.keySet());
		java.util.Collections.sort(countries);
		double imps = 0.0;
		double reus = 0.0;
		for (String country : countries) {
			Country c = graphs[0].getCountry(country);
//			double bending = bendings.getDouble(country);
//			double[] reuses = calculateReuse(country);
//			double reuse = new Mean().evaluate(reuses);
//			double variance = new Variance().evaluate(reuses);
//			double covariance = new Covariance().covariance(levels, reuses);
//			System.out.println(Formatter.toTabs(country, bending, reuse, variance, covariance));
//			System.out.println(Formatter.toTabs(country, graphs[0].getCountry(country).getExports(), graphs[0].getCountry(country).getImports(), graphs[0].getCountry(country).getConsumption(), graphs[0].getCountry(country).getMaxDomesticFlow(true)));
			System.out.println(year + "\t" + country + "\t" + c.getImportReuse());
			imps += c.getExports();
			reus += c.getReusedImports();
		}
		System.out.println(year + "\tGlobal\t" + (reus / imps));
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
		for (int year = 1995; year <= 2011; year++) {
			BendingByCountry bendings = new BendingByCountry(year);
//			bendings.optimizeAll();
			bendings.printAll();
		}
	}

}
