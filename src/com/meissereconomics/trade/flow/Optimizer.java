package com.meissereconomics.trade.flow;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

import com.meissereconomics.trade.data.InputOutputGraph;
import com.meissereconomics.trade.data.WiodInputOutputGraph;
import com.meissereconomics.trade.graph.Country;
import com.meissereconomics.trade.graph.EFlowBendingMode;
import com.meissereconomics.trade.util.Average;
import com.meissereconomics.trade.util.Formatter;

public class Optimizer {

	private static final double EPSILON = 0.001;
	private static final double BENDING_EPSILON = 0.0001;

	private double[] levels;
	private InputOutputGraph[][] graphs;
	
	public Optimizer(InputOutputGraph graph, int seed, int runs) {
		this(graph, null, seed, runs, false);
	}

	public Optimizer(InputOutputGraph graph, String country, int seed, int runs, boolean fastAndDirty) {
		this.levels = new double[graph.getSectors()];
		this.graphs = new InputOutputGraph[levels.length][runs];
		if (fastAndDirty) {
			graph.collapseRandomSectors(13, 1, country);
		}
		for (int i = 0; i < graphs.length; i++) {
			int sector = i + 1;
			this.levels[i] = sector;
			for (int run = 0; run < runs; run++) {
				InputOutputGraph g = graph.copy();
				g.collapseRandomSectors(run * 31 + seed, sector);
				g.deriveOrigins(EFlowBendingMode.DEFAULT, 0.0);
				graphs[i][run] = g;
			}
		}
	}

	public double minimizeError(String country, EFlowBendingMode mode) {
		return threePointSearch(country, mode, 0.0, 1.0);
	}
	
	public double minimizeCovariance(String country, EFlowBendingMode mode) {
		return binarySearch(country, mode, 0.0, 1.0);
	}

	private double binarySearch(String country, EFlowBendingMode mode, double bending1, double bending2) {
		if (bending1 > bending2) {
			return binarySearch(country, mode, bending2, bending1);
		} else {
			double cov1 = calculateCovariance(country, mode, bending1);
			if (cov1 <= 0.0) {
				return bending1;
			}
			double cov2 = calculateCovariance(country, mode, bending2);
			if (cov2 >= 0.0) {
				return bending2;
			}
			while (Math.abs(bending2 - bending1) > EPSILON) {
				double middle = (bending1 + bending2) / 2;
				double covMiddle = calculateCovariance(country, mode, middle);
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

	protected double calculateCovariance(String country, EFlowBendingMode mode, double bending) {
		setBending(country, mode, bending);
		double[] avgs = average(calculateReuse(country)); 
		double cov = new Covariance().covariance(levels, avgs);
		return cov;
	}

	private double threePointSearch(String country, EFlowBendingMode mode, double left, double right) {
		if (left > right) {
			return threePointSearch(country, mode, right, left);
		} else {
			double middle = (right + left) / 2;
			double middleValue = calculateError(country, mode, middle);
			while (right - left > EPSILON) {
				boolean addRight = right - middle > middle - left;
				double middleLeft = addRight ? middle : (left + middle) / 2;
				double middleLeftValue = addRight ? middleValue : calculateError(country, mode, middleLeft);
				double middleRight = addRight ? (right + middle) / 2 : middle;
				double middleRightValue = addRight ? calculateError(country, mode, middleRight) : middleValue;
				if (middleLeftValue < middleRightValue) {
					middle = middleLeft;
					middleValue = middleLeftValue;
					right = middleRight;
				} else {
					middle = middleRight;
					middleValue = middleRightValue;
					left = middleLeft;
				}
			}
			return middle;
		}
	}

	protected double calculateError(String country, EFlowBendingMode mode, double bending) {
		setBending(country, mode, bending);
		double[] avgs = average(calculateReuse(country));
		double mean = average(avgs);
		return calcError(avgs, mean);
	}

	private double calcError(double[] avgs, double mean) {
		double error = 0.0;
		for (double avg : avgs) {
			double deviation = avg - mean;
			error += deviation * deviation;
		}
		return error;
	}

	private double[] average(double[][] raw) {
		double[] avgs = new double[raw.length];
		for (int i = 0; i < avgs.length; i++) {
			avgs[i] = average(raw[i]);
		}
		return avgs;
	}

	private double average(double[] ds) {
		double sum = 0.0;
		for (double d : ds) {
			sum += d;
		}
		return sum / ds.length;
	}

	protected void setBending(String country, EFlowBendingMode mode, double bending) {
		for (InputOutputGraph[] graphs : this.graphs) {
			for (InputOutputGraph graph : graphs) {
				graph.getCountry(country).deriveOrigins(mode, bending, BENDING_EPSILON);
			}
		}
	}

	protected double[][] calculateReuse(String country) {
		double[][] reuse = new double[levels.length][];
		for (int i = 0; i < reuse.length; i++) {
			reuse[i] = calculateReuse(country, graphs[i]);
		}
		return reuse;
	}

	private double[] calculateReuse(String country, InputOutputGraph[] inputOutputGraphs) {
		double[] res = new double[inputOutputGraphs.length];
		for (int i = 0; i < res.length; i++) {
			res[i] = inputOutputGraphs[i].getCountry(country).getImportReuse();
		}
		return res;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		System.out.println("Country\tmode\tbending\tvar\tcov\tstdDev\tSectors...");
		InputOutputGraph graph = new WiodInputOutputGraph(2008);
		Optimizer test = new Optimizer(graph.copy(), 5323431, 5);
		for (Country country : graph.getCountries()) {
			// EFlowBendingMode mode = EFlowBendingMode.DEFAULT;
			for (EFlowBendingMode mode : EFlowBendingMode.values()) {
				double bending = test.minimizeCovariance(country.getName(), mode);
				test.print(country.getName(), mode.name(), bending);
			}
			test.setBending(country.getName(), EFlowBendingMode.DEFAULT, 0.0);
			test.print(country.getName(), "Leontief", 0.0);
		}
	}

	protected void print(String country, String mode, double bending) {
		double[][] reuse = calculateReuse(country);
		double[] avg = average(reuse);
		double var = new Variance().evaluate(avg);
		double cov = new Covariance().covariance(levels, avg);
		double stdDev = calcAvgStdDev(reuse);
		System.out.println(country + "\t" + mode + "\t" + bending + "\t" + var + "\t" + cov + "\t" + stdDev + "\t" + Formatter.toTabs(avg));
	}

	private static double calcAvgStdDev(double[][] reuse) {
		int runs = reuse[1].length;
		for (double[] r : reuse) {
			assert r.length == 1 || r.length == runs;
		}
		double sum = 0.0;
		for (int run = 0; run < runs; run++) {
			Average avg = new Average();
			for (int level = 0; level < reuse.length; level++) {
				double[] currentLevelResults = reuse[level];
				avg.add(currentLevelResults.length == 1 ? currentLevelResults[0] : currentLevelResults[run]);
			}
			sum += Math.sqrt(avg.getVariance());
		}
		return sum / runs;
	}

}
