package com.meissereconomics.seminar.run;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.meissereconomics.seminar.Country;
import com.meissereconomics.seminar.EFlowBendingMode;
import com.meissereconomics.seminar.InputOutputGraph;
import com.meissereconomics.seminar.util.Table;

/**
 * Running this script should produce output "Figure1Resolution.out"
 */
public class ConsumptionPreference {

	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		long t0 = System.nanoTime();
		Table results = new Table("Mode", "Level", "Country", "Bending");
		EFlowBendingMode mode = EFlowBendingMode.DEFAULT;
		results.setCurrent("Mode", mode.name());
		for (int run = 0; run < 10; run++) {
			InputOutputGraph iograph = new InputOutputGraph("data/wiot11_row_sep12.CSV");
			for (int level = InputOutputGraph.SECTORS; level > 0; level--) {
				results.setCurrent("Level", Integer.toString(level));
				iograph.collapseRandomSectors(run * 31, level);
				for (double bending = 0.0; bending <= 1.0; bending += 0.1) {
					results.setCurrent("Bending", Double.toString(bending));
					iograph.deriveOrigins(mode, bending);

					for (Country c : iograph.getCountries()) {
						results.setCurrent("Country", c.toString());
						results.include("Reuse", c.getReusedImports() / c.getExports());
					}
					results.setCurrent("Country", "Global");
					results.include("Reuse", iograph.getGlobalImportReuse());
				}
				long deltaT = System.nanoTime() - t0;
				System.out.println("Level " + level + " completed after " + deltaT / 1000000 + "ms");
			}
		}
		results.printAll();
	}

}
