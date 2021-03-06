package com.meissereconomics.trade.run;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

import com.meissereconomics.trade.data.InputOutputGraph;
import com.meissereconomics.trade.data.OldWiodInputOutputGraph;
import com.meissereconomics.trade.graph.Country;
import com.meissereconomics.trade.graph.EFlowBendingMode;
import com.meissereconomics.trade.util.Average;
import com.meissereconomics.trade.util.Formatter;

public class CountryPreferenceTest {

	private static final int RUNS = 5;

	private static final double EPSILON = 0.001;

	String country;
	private double[] levels;
	private InputOutputGraph[][] graphs;

	public CountryPreferenceTest(InputOutputGraph graph, String country, int seed, int runs) throws FileNotFoundException, IOException {
		this.country = country;
		this.levels = new double[graph.getSectors()];
		this.graphs = new InputOutputGraph[levels.length][runs];
		System.out.print("Loading " + country + " " + runs + " times..");
		for (int i = 0; i < graphs.length; i++) {
			int sector = i + 1;
			this.levels[i] = sector;
			for (int run = 0; run < runs; run++) {
				InputOutputGraph g = graph.copy();
				g.collapseRandomSectors(run * 31 + seed, 1, country);
				g.getCountry(country).collapseRandomSectors(run * 31 + seed, sector);
				g.deriveOrigins(EFlowBendingMode.DEFAULT, 0.0);
				graphs[i][run] = g;
			}
			System.out.print(".");
		}
		System.out.println("...done.");
	}

	public double minimize(EFlowBendingMode mode) {
		return binarySearch(mode, 0.0, 1.0);
	}

	private double binarySearch(EFlowBendingMode mode, double bending1, double bending2) {
		if (bending1 > bending2) {
			return binarySearch(mode, bending2, bending1);
		} else {
			double cov1 = calculateCovariance(mode, bending1);
			if (cov1 <= 0.0) {
				return bending1;
			}
			double cov2 = calculateCovariance(mode, bending2);
			if (cov2 >= 0.0) {
				return bending2;
			}
			while (Math.abs(bending2 - bending1) > EPSILON) {
				double middle = (bending1 + bending2) / 2;
				double covMiddle = calculateCovariance(mode, middle);
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

	protected double calculateCovariance(EFlowBendingMode mode, double bending) {
		setBending(mode, bending);
		double[] avgs = average(calculateReuse()); 
		double cov = new Covariance().covariance(levels, avgs);
		return cov;
	}

	private double[] average(double[][] raw) {
		double[] avgs = new double[raw.length];
		for (int i=0; i<avgs.length; i++){
			avgs[i] = average(raw[i]);
		}
		return avgs;
	}

	private double average(double[] ds) {
		double sum = 0.0;
		for (double d: ds){
			sum += d;
		}
		return sum / ds.length;
	}

	protected void setBending(EFlowBendingMode mode, double bending) {
		for (InputOutputGraph[] graphs : this.graphs) {
			for (InputOutputGraph graph : graphs) {
				graph.getCountry(country).deriveOrigins(mode, bending, EPSILON);
			}
		}
	}

	protected double[][] calculateReuse() {
		double[][] reuse = new double[levels.length][];
		for (int i = 0; i < reuse.length; i++) {
			reuse[i] = calculateReuse(country, graphs[i]);
		}
		return reuse;
	}

	private double[] calculateReuse(String country, InputOutputGraph[] inputOutputGraphs) {
		double[] res = new double[inputOutputGraphs.length];
		for (int i=0; i<res.length; i++){
			res[i] = inputOutputGraphs[i].getCountry(country).getImportReuse();
		}
		return res;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		InputOutputGraph original = new OldWiodInputOutputGraph(2011);
		for (Country country : original.getCountries()) {
			InputOutputGraph graph = original.copy();
			CountryPreferenceTest test = new CountryPreferenceTest(graph, country.getName(), 532431 ^ country.getName().hashCode(), RUNS);
			for (EFlowBendingMode mode : EFlowBendingMode.values()) {
				double bending = test.minimize(mode);
				test.print(mode.name(), bending);
			}
			test.setBending(EFlowBendingMode.DEFAULT, 0.0);
			test.print("Leontief", 0.0);
		}
	}

	protected void print(String mode, double bending) {
		double[][] reuse = calculateReuse();
		double[] avg = average(reuse);
		double var = new Variance().evaluate(avg);
		double cov = new Covariance().covariance(levels, avg);
		double stdDev = calcAvgStdDev(reuse);
		System.out.println(country + "\t" + mode + "\t" + bending + "\t" + var + "\t" + cov + "\t" + stdDev + "\t" + Formatter.toTabs(avg));
	}

	private static double calcAvgStdDev(double[][] reuse) {
		int runs = reuse[1].length;
		for (double[] r: reuse){
			assert r.length == 1 || r.length == runs;
		}
		double sum = 0.0;
		for (int run=0; run<runs; run++){
			Average avg = new Average();
			for (int level=0; level<reuse.length; level++){
				double[] currentLevelResults = reuse[level];
				avg.add(currentLevelResults.length == 1 ? currentLevelResults[0] : currentLevelResults[run]);
			}
			sum += Math.sqrt(avg.getVariance());
		}
		return sum / runs;
	}

}
