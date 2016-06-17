package com.meissereconomics.seminar.run;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.function.BiConsumer;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.util.DoubleArray;
import org.apache.commons.math3.util.ResizableDoubleArray;

import com.meissereconomics.seminar.EFlowBendingMode;
import com.meissereconomics.seminar.data.InputOutputGraph;
import com.meissereconomics.seminar.data.WiodInputOutputGraph;
import com.meissereconomics.seminar.util.InstantiatingHashmap;

/**
 * Running this script should produce output "Figure1Resolution.out"
 */
public class Figure1Resolution {

	private static InstantiatingHashmap<Integer, DoubleArray> createMap() {
		return new InstantiatingHashmap<Integer, DoubleArray>() {

			@Override
			protected DoubleArray createValue(Integer key) {
				return new ResizableDoubleArray();
			}

		};
	}

	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		printResolutionGraph(new WiodInputOutputGraph("data/wiot11_row_sep12.CSV"));
//		printResolutionGraph(new USGraph(true));
	}

	protected static void printResolutionGraph(InputOutputGraph original) throws FileNotFoundException, IOException {
		long t0 = System.nanoTime();
		InstantiatingHashmap<Integer, DoubleArray> inputReuse = createMap();
		for (int run = 0; run < 10; run++) {
			InputOutputGraph graph = original.copy();
			int sectors = graph.getSectors();
			for (int level = sectors; level > 0; level--) {
				graph.collapseRandomSectors(run * 31, level);
				graph.deriveOrigins(EFlowBendingMode.DEFAULT, 0.0);
				double reuse = graph.getGlobalImportReuse();
				inputReuse.obtain(level).addElement(reuse);
			}
			long deltaT = System.nanoTime() - t0;
			System.out.println("Run " + run + " completed after " + deltaT / 1000000 + "ms");
		}
		System.out.println("Resolution\tMean Import Reuse\tVariance");
		inputReuse.forEach(new BiConsumer<Integer, DoubleArray>() {

			@Override
			public void accept(Integer t, DoubleArray u) {
				double[] samples = u.getElements();
				System.out.println(t + "\t" + new Mean().evaluate(samples) + "\t" + new Variance().evaluate(samples));
			}
		});
	}

}
