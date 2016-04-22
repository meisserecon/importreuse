package com.meissereconomics.seminar.run;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.function.BiConsumer;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.util.DoubleArray;
import org.apache.commons.math3.util.ResizableDoubleArray;

import com.meissereconomics.seminar.InputOutputGraph;
import com.meissereconomics.seminar.util.InstantiatingHashmap;

/** 
 *  Running this script should produce output "Figure1Resolution.out"
 */
public class Figure1Resolution {

	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		long t0 = System.nanoTime();
		InstantiatingHashmap<Integer, DoubleArray> levels = new InstantiatingHashmap<Integer, DoubleArray>(){

			@Override
			protected DoubleArray createValue(Integer key) {
				return new ResizableDoubleArray();
			}
			
		};
		for (int run = 0; run < 100; run++) {
			InputOutputGraph iograph = new InputOutputGraph("data/wiot05_row_apr12.CSV");
			for (int level = InputOutputGraph.SECTORS; level > 0; level--) {
				iograph.collapseRandomSectors(run * 31, level);
				iograph.deriveOrigins();
				double reuse = iograph.getGlobalImportReuse();
				levels.obtain(level).addElement(reuse);
			}
			long deltaT = System.nanoTime() - t0;
			System.out.println("Run " + run + " completed after " + deltaT / 1000000 + "ms");
		}
		System.out.println("Resolution\tMean Import Reuse\tVariance");
		levels.forEach(new BiConsumer<Integer, DoubleArray>() {

			@Override
			public void accept(Integer t, DoubleArray u) {
				double[] samples = u.getElements();
				System.out.println(t + "\t" + new Mean().evaluate(samples) + "\t" + new Variance().evaluate(samples));
			}
		});
		
	}

}
