package com.meissereconomics.seminar;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.function.BiConsumer;

import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.util.DoubleArray;
import org.apache.commons.math3.util.ResizableDoubleArray;

import com.meissereconomics.seminar.util.InstantiatingHashmap;

public class ImportExportCorrelation {

	private InstantiatingHashmap<Integer, DoubleArray[]> bins;

	public ImportExportCorrelation() {
		this.bins = new InstantiatingHashmap<Integer, DoubleArray[]>() {

			@Override
			protected DoubleArray[] createValue(Integer key) {
				return new ResizableDoubleArray[] { new ResizableDoubleArray(), new ResizableDoubleArray() };
			}
		};
	}

	public void fill(int seed) throws FileNotFoundException, IOException {
		InputOutputGraph iograph = new InputOutputGraph("data/wiod-2005.CSV");
		for (int level = InputOutputGraph.SECTORS; level > 0; level--) {
			iograph.collapseRandomSectors(seed * 31, level);
			for (Country c : iograph.getCountries()) {
				final int index = level - 1;
				collectIntensities(c);
			}
		}
	}

	private void collectIntensities(Country c) {
		for (Node n : c.getNodes()) {
			if (!n.isConsumption()) {
				int bin = (int) Math.log(n.getOutputs() + n.getInputs());
				DoubleArray[] both = bins.obtain(bin);
				both[0].addElement(n.getIntensity(true));
				both[1].addElement(n.getIntensity(false));
			}
		}
	}

	public void printCorrelations() {
		System.out.println("Level\tSamples\tMean Import Intensity\tImport Variance\tMean Export Intensity\tExport Variance\tCorrelation\tCovariance");
		bins.forEach(new BiConsumer<Integer, DoubleArray[]>() {

			@Override
			public void accept(Integer t, DoubleArray[] u) {
				double[] imp = u[0].getElements();
				double[] exp = u[1].getElements();
				double meanImp = new Mean().evaluate(imp);
				double meanExp = new Mean().evaluate(exp);
				double varImp = new Variance().evaluate(imp, meanImp);
				double varExp = new Variance().evaluate(exp, meanExp);
				double correlation = new PearsonsCorrelation().correlation(imp, exp);
				double covariance = new Covariance().covariance(imp, exp);
				System.out.println(t + "\t" + (imp.length + exp.length) + "\t" + meanImp + "\t" + varImp + "\t" + meanExp + "\t" + varExp + "\t" + correlation + "\t" + covariance);
			}
		});
	}

	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		// System.out.println("Run\tSectors\tReuse");
		long t0 = System.nanoTime();
		ImportExportCorrelation cor = new ImportExportCorrelation();
		for (int run = 0; run < 10; run++) {
			cor.fill(run * 31);
		}
		cor.printCorrelations();
		long deltaT = System.nanoTime() - t0;
		// System.out.println("Took " + deltaT / 1000000 + "ms");
	}

}
