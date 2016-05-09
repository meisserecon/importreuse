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

import net.openhft.koloboke.collect.map.ObjDoubleMap;
import net.openhft.koloboke.collect.map.hash.HashObjDoubleMaps;

/**
 */
public class ConsumptionPreferenceOverTime {

	private static final double DEFAULT_BENDING = 0.0;
	private static final double EPSILON = 0.001;
	private static final EFlowBendingMode MODE = EFlowBendingMode.DEFAULT;

	private int year;
	private double[] levels;
	private InputOutputGraph[][] graphs;
	private ObjDoubleMap<String> bendings, leontief;

	public ConsumptionPreferenceOverTime(int seed, int year, int runs) throws FileNotFoundException, IOException {
		this.year = year;
		String file = getFilename(year);
		// System.out.println("Processing file " + file);
		this.levels = new double[InputOutputGraph.SECTORS];
		this.bendings = HashObjDoubleMaps.newMutableMap();
		this.leontief = HashObjDoubleMaps.newMutableMap();
		this.bendings = HashObjDoubleMaps.newMutableMap();
		this.graphs = new InputOutputGraph[InputOutputGraph.SECTORS][runs];
		for (int i = 0; i < graphs.length; i++) {
			int sector = i + 1;
			this.levels[i] = sector;
			for (int run = 0; run < runs; run++) {
				System.out.println("Loading " + sector + " - " + run);
				InputOutputGraph graph = new InputOutputGraph(file);
				graph.collapseRandomSectors(run * 31 + seed * 12313, sector);
				if (sector == InputOutputGraph.SECTORS && run == 0) {
					graph.deriveOrigins(MODE, 0.0);
					for (Country c : graph.getCountries()) {
						this.leontief.put(c.getName(), c.getImportReuse());
						this.bendings.put(c.getName(), DEFAULT_BENDING);
					}
				}
				graph.deriveOrigins(MODE, DEFAULT_BENDING);
				if (sector == InputOutputGraph.SECTORS || sector == 1){
					// graph 1 and 35 are always the same
					this.graphs[i] = new InputOutputGraph[]{graph};
					break;
				} else {
					this.graphs[i][run] = graph;
				}
			}
		}
	}

	private static String getFilename(int year) {
		if (year <= 1999) {
			return "data/wiot" + (year - 1900) + "_row_apr12.csv";
		} else if (year <= 2007) {
			return "data/wiot0" + (year - 2000) + "_row_apr12.csv";
		} else if (year <= 2009) {
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
		ArrayList<String> countries = new ArrayList<>(bendings.keySet());
		java.util.Collections.sort(countries);
		for (String country : countries) {
			double bending = bendings.getDouble(country);
			double[] reuses = calculateReuse(country);
			double reuse = new Mean().evaluate(reuses);
			double variance = new Variance().evaluate(reuses);
			double covariance = new Covariance().covariance(levels, reuses);
			double leontiefReuse = leontief.getDouble(country);
			Country c = graphs[0][0].getCountry(country);
			double exports = c.getExports();
			System.out.println(Formatter.toTabs(year, country, bending, reuse, variance, covariance, exports, c.getImports(), c.getConsumption(), c.getMaxDomesticFlow(true) / exports, leontiefReuse));
		}
	}

	public void optimizeCountry(String country) {
		double bending1 = bendings.getDouble(country);
		double cov1 = calculateCovariance(country, bending1);
		if (Math.abs(cov1) > EPSILON) {
			double bending2 = findFirst(country, bending1, cov1 > 0);
			double bendingNew = bending2 <= 0.0 ? 0.0 : binarySearch(country, bending1, bending2);
			// System.out.println("Updating bending of " + country + " from " + bending1 + " to " + bendingNew);
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
			if (cov2 >= 0.0) {
				return bending2;
			}
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
		for (InputOutputGraph[] graphs : this.graphs) {
			for (InputOutputGraph graph : graphs) {
				graph.getCountry(country).deriveOrigins(MODE, bending);
			}
		}
		double cov = new Covariance().covariance(levels, calculateReuse(country));
		return cov;
	}

	protected double[] calculateReuse(String country) {
		double[] reuse = new double[levels.length];
		for (int i = 0; i < reuse.length; i++) {
			reuse[i] = calculateReuse(country, graphs[i]);
		}
		return reuse;
	}

	private double calculateReuse(String country, InputOutputGraph[] inputOutputGraphs) {
		double avg = 0.0;
		for (InputOutputGraph graph: inputOutputGraphs){
			avg += graph.getCountry(country).getImportReuse();
		}
		return avg / inputOutputGraphs.length;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		long t0 = System.nanoTime();
		System.out.println("Year\tCountry\tBending\tReuse\tVariance\tCovariance\tExports\tImports\tConsumption\tMax flow reuse\tLeontief Reuse");
		for (int year = 1995; year <= 2011; year++) {
			ConsumptionPreferenceOverTime bendings = new ConsumptionPreferenceOverTime(13, year, 5);
			bendings.optimizeAll();
			bendings.printAll();
			System.out.println("Processed " + year + " after " + (System.nanoTime() - t0)/1000000 + "ms");
		}
	}

}
