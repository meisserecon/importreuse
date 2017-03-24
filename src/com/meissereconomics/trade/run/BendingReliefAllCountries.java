package com.meissereconomics.trade.run;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.meissereconomics.trade.Country;
import com.meissereconomics.trade.EFlowBendingMode;
import com.meissereconomics.trade.data.InputOutputGraph;
import com.meissereconomics.trade.data.OldWiodInputOutputGraph;
import com.meissereconomics.trade.util.Table;

/**
 * Running this script should produce output "BendingReliefAllCountries.out"
 */
public class BendingReliefAllCountries {

	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		long t0 = System.nanoTime();
		Table results = new Table("Mode", "Sectors", "Country", "Preference");
		EFlowBendingMode mode = EFlowBendingMode.DEFAULT;
		results.setCurrent("Mode", mode.name());
		for (int run = 0; run < 20; run++) {
			InputOutputGraph iograph = new OldWiodInputOutputGraph("data/wiot11_row_sep12.CSV");
			for (int level = OldWiodInputOutputGraph.SECTORS; level > 0; level--) {
				results.setCurrent("Sectors", Integer.toString(level));
				iograph.collapseRandomSectors(run * 31, level);

				for (double pref = 0.0; pref <= 1.01; pref += 0.1) {
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
					for (Country c : iograph.getCountries()) {
						results.setCurrent("Country", c.toString());
						double maxflow = c.getMaxReusedImports();
						double minflow = c.getMinReusedImports();
						assert maxflow >= minflow;
						double exports = c.getExports();
						results.setCurrent("Preference", "Max Flow");
						results.include("Reuse", maxflow / exports);
						results.setCurrent("Preference", "Min Flow");
						results.include("Reuse", minflow / exports);
					}
				}

				long deltaT = System.nanoTime() - t0;
				System.out.println("Level " + level + " completed after " + deltaT / 1000000 + "ms");
			}
		}
		results.printAll();
	}

}
