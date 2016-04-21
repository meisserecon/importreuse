package com.meissereconomics.seminar.run;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.function.BiConsumer;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.util.DoubleArray;
import org.apache.commons.math3.util.ResizableDoubleArray;

import com.meissereconomics.seminar.InputOutputGraph;
import com.meissereconomics.seminar.util.InstantiatingHashmap;

public class Figure1Resolution {
	
	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		// System.out.println("Run\tSectors\tReuse");
		long t0 = System.nanoTime();
		InstantiatingHashmap<Integer, DoubleArray> levels = new InstantiatingHashmap<Integer, DoubleArray>(){

			@Override
			protected DoubleArray createValue(Integer key) {
				return new ResizableDoubleArray();
			}
			
		};
		for (int run = 0; run < 10; run++) {
			InputOutputGraph iograph = new InputOutputGraph("data/wiot05_row_apr12.CSV");
			for (int level = InputOutputGraph.SECTORS; level > 0; level--) {
				// for (Country c : iograph.getCountries()) {
				// System.out.println(c.getStats());
				// }
				iograph.collapseRandomSectors(run * 31, level);
				iograph.deriveOrigins();
				double reuse = iograph.getGlobalImportReuse();
				levels.obtain(level).addElement(reuse);
			}
			long deltaT = System.nanoTime() - t0;
			System.out.println("Run " + run + " completed after " + deltaT / 1000000 + "ms");
		}
		System.out.println("Resolution\tImport Reuse");
		levels.forEach(new BiConsumer<Integer, DoubleArray>() {

			@Override
			public void accept(Integer t, DoubleArray u) {
				System.out.println(t + "\t" + new Mean().evaluate(u.getElements()));
			}
		});
		
	}

}
