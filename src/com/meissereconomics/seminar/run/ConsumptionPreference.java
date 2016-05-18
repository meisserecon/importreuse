package com.meissereconomics.seminar.run;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.meissereconomics.seminar.Country;
import com.meissereconomics.seminar.EFlowBendingMode;
import com.meissereconomics.seminar.InputOutputGraph;
import com.meissereconomics.seminar.util.Table;

/**
 * Running this script should produce output "ConsumptionPreference.out"
 */
public class ConsumptionPreference {

	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		long t0 = System.nanoTime();
		Table results = new Table("Mode", "Sectors", "Country", "Preference");
		EFlowBendingMode mode = EFlowBendingMode.DEFAULT;
		results.setCurrent("Mode", mode.name());
		for (int run = 0; run < 50; run++) {
			InputOutputGraph iograph = new InputOutputGraph("data/wiot11_row_sep12.CSV");
			for (int level = InputOutputGraph.SECTORS; level > 0; level--) {
				results.setCurrent("Sectors", Integer.toString(level));
				iograph.collapseRandomSectors(run * 31, level);

				for (double pref = 0.0; pref <= 1.01; pref += 0.05) {
					results.setCurrent("Preference", Double.toString(pref));
					iograph.deriveOrigins(mode, pref);

					for (Country c : iograph.getCountries()) {
						results.setCurrent("Country", c.toString());
						results.include("Reuse", c.getReusedImports() / c.getExports());
					}
					results.setCurrent("Country", "Global");
					results.include("Reuse", iograph.getGlobalImportReuse());
				}
				{
					results.setCurrent("Preference", "Max Flow");

					for (Country c : iograph.getCountries()) {
						results.setCurrent("Country", c.toString());
						double maxflow = c.getMaxReusedImports();
						double minflow = c.getMinReusedImports();
						assert maxflow >= minflow;
						double exports = c.getExports();
						results.include("Max Reuse", maxflow / exports);
						results.include("Min Reuse", minflow / exports);
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
