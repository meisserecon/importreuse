package com.meissereconomics.seminar.run;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

import com.meissereconomics.seminar.Country;
import com.meissereconomics.seminar.EFlowBendingMode;
import com.meissereconomics.seminar.InputOutputGraph;
import com.meissereconomics.seminar.util.Formatter;

public class CountryPreferenceTestLarge {

	private static final int RUNS = 20;

	private static final double EPSILON = 0.00001;

	private String country;
	private double[] levels;
	private InputOutputGraph[][] graphs;

	public CountryPreferenceTestLarge(String country, int year, int seed) throws FileNotFoundException, IOException {
		this.country = country;
		this.levels = new double[InputOutputGraph.SECTORS];
		this.graphs = new InputOutputGraph[InputOutputGraph.SECTORS][RUNS];
		for (int i = 0; i < graphs.length; i++) {
			int sector = i + 1;
			this.levels[i] = sector;
			for (int run = 0; run < RUNS; run++) {
				InputOutputGraph g = new InputOutputGraph(Formatter.getFilename(year));
				g.collapseRandomSectors(run * 31 + seed, 1, country);
				g.getCountry(country).collapseRandomSectors(run * 31 + seed, sector);
				g.deriveOrigins(EFlowBendingMode.DEFAULT, 0.0);
				graphs[i][run] = g;
			}
			System.out.println("Loaded level " + sector);
		}
		System.out.println(country + "\tBending\tVar\tCov");
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
		double cov = new Covariance().covariance(levels, calculateReuse());
		return cov;
	}

	protected void setBending(EFlowBendingMode mode, double bending) {
		for (InputOutputGraph[] graphs : this.graphs) {
			for (InputOutputGraph graph : graphs) {
				graph.getCountry(country).deriveOrigins(mode, bending, EPSILON);
			}
		}
	}

	protected double[] calculateReuse() {
		double[] reuse = new double[levels.length];
		for (int i = 0; i < reuse.length; i++) {
			reuse[i] = calculateReuse(country, graphs[i]);
		}
		return reuse;
	}

	private double calculateReuse(String country, InputOutputGraph[] inputOutputGraphs) {
		double avg = 0.0;
		for (InputOutputGraph graph : inputOutputGraphs) {
			avg += graph.getCountry(country).getImportReuse();
		}
		return avg / inputOutputGraphs.length;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		CountryPreferenceTestLarge test = new CountryPreferenceTestLarge("USA", 2011, 94357);
		for (EFlowBendingMode mode : EFlowBendingMode.values()) {
			double optimalBending = test.minimize(mode);
			print(test, mode, optimalBending);
			for (int i = 0; i <= 20; i++) {
				double bending = i * 0.05;
				print(test, mode, bending);
			}
		}
		print("Max Flow", 0.0, test.levels, test.calcMaxFlowReuse(false));
		print("Max Flow", 1.0, test.levels, test.calcMaxFlowReuse(true));
	}

	public double[] calcMaxFlowReuse(boolean max) {
		double[] reuse = new double[levels.length];
		for (int i = 0; i < reuse.length; i++) {
			double sum = 0.0;
			for (int run = 0; run < graphs[i].length; run++) {
				Country c = graphs[i][0].getCountry(country);
				sum += (max ? c.getMaxReusedImports() : c.getMinReusedImports()) / c.getExports();
			}
			reuse[i] = sum / graphs[i].length;
		}
		return reuse;
	}

	protected static void print(CountryPreferenceTestLarge test, EFlowBendingMode mode, double bending) {
		test.setBending(mode, bending);
		double[] reuse = test.calculateReuse();
		print(mode.name(), bending, test.levels, reuse);
	}

	protected static void print(String mode, double bending, double[] levels, double[] reuse) {
		double var = new Variance().evaluate(reuse);
		double cov = new Covariance().covariance(levels, reuse);
		System.out.println(mode + "\t" + bending + "\t" + var + "\t" + cov + "\t" + Formatter.toTabs(reuse));
	}

}
