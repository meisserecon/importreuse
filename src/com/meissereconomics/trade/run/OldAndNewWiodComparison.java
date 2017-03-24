package com.meissereconomics.trade.run;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.meissereconomics.trade.EFlowBendingMode;
import com.meissereconomics.trade.data.InputOutputGraph;
import com.meissereconomics.trade.data.OldWiodInputOutputGraph;
import com.meissereconomics.trade.data.WiodInputOutputGraph;
import com.meissereconomics.trade.util.Formatter;
import com.meissereconomics.trade.util.Timer;

public class OldAndNewWiodComparison {

	private ExecutorService executor;

	public OldAndNewWiodComparison() {
		this.executor = Executors.newFixedThreadPool(2);
	}

	public void compare() throws InterruptedException, ExecutionException {
		System.out.println(Formatter.toTabs("Year", "WIOD 2013 leontief", "WIOD 2013 scale-free", "WIOD 2016 leontief",
				"WIOD 2016 scale-free"));
		Timer timer = new Timer();
		ArrayList<Future<String>> futures = new ArrayList<>();
		for (int year = 2000; year <= 2011; year++) {
			final int yearFinal = year;
			final CompletableFuture<String> future = new CompletableFuture<>();
			futures.add(future);
			executor.submit(new Runnable() {

				@Override
				public void run() {
					try {
						double[] reuseOld = deriveImportReuse(new OldWiodInputOutputGraph(yearFinal));
						double[] reuseNew = deriveImportReuse(new WiodInputOutputGraph(yearFinal));
						future.complete(Formatter.toTabs(Integer.toString(yearFinal), reuseOld[0], reuseOld[1],
								reuseNew[0], reuseNew[1]));
					} catch (IOException e) {
						future.completeExceptionally(e);
					}
				}
			});
		}
		for (Future<String> f : futures) {
			System.out.println(f.get());
		}
		timer.time("complete");
	}

	private double[] deriveImportReuse(InputOutputGraph graph1) throws FileNotFoundException, IOException {
		graph1.deriveOrigins(EFlowBendingMode.DEFAULT, 0.0);
		double leontief = graph1.getGlobalImportReuse();
		GlobalImportReuse global = new GlobalImportReuse(graph1, 13, 3);
		double bending = global.minimize(EFlowBendingMode.DEFAULT);
		graph1.deriveOrigins(EFlowBendingMode.DEFAULT, bending);
		double extrapolated = graph1.getGlobalImportReuse();
		return new double[] { leontief, extrapolated };
	}

	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException, ExecutionException {
		new OldAndNewWiodComparison().compare();
	}

	// Year WIOD 2013 Reuse WIOD 2016 Reuse
	// 2000 0.2131988644498795 0.21670921901884352
	// 2001 0.210643572526784 0.21510158383174324
	// 2002 0.21134166226703238 0.20990344487042764
	// 2003 0.21587038561903912 0.2153671633193332
	// 2004 0.2257440832465289 0.2309593919972352
	// 2005 0.2300119650984833 0.23844254214974614
	// 2006 0.23893760358873128 0.24997701304341113
	// 2007 0.24329512778902287 0.2539907493381569
	// 2008 0.2478918137086482 0.2639032206841105
	// 2009 0.22617409861699406 0.22997075046827042
	// 2010 0.24140382802541147 0.24482523121055358
	// 2011 0.25150648262924397 0.25609752668834584

}
