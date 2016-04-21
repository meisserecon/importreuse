package com.meissereconomics.seminar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ParallelizedTask extends Thread {

	private int seed;
	private int repetitions;
	private CompletableFuture<Result> res;

	public ParallelizedTask(int seed, int repetitions) {
		super("Worker");
		this.seed = seed;
		this.repetitions = repetitions;
		this.res = new CompletableFuture<>();
		this.setDaemon(true);
		this.start();
	}

	@Override
	public void run() {
		try {
			Result res = new Result(repetitions);
			for (int i = 0; i < repetitions; i++) {
				InputOutputGraph iograph = new InputOutputGraph("data/wiod-2005.CSV");
				// for (Country c : iograph.getCountries()) {
				// System.out.println(c.getStats());
				// }
				iograph.collapseRandomSectors(seed * 31, i + 1);
				iograph.deriveOrigins();
				double reuse = iograph.getGlobalImportReuse();
				res.append(reuse);
				System.out.println(reuse);
			}
			synchronized (res) {
				this.res.complete(res);
			}
		} catch (IOException e) {
			synchronized (res) {
				this.res.completeExceptionally(e);
			}
		}
	}

	public Result getResult() throws InterruptedException, ExecutionException {
		return res.get();
	}

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		long t0 = System.nanoTime();
		ArrayList<ParallelizedTask> tasks = new ArrayList<>();
		int repetitions = 5;
		for (int i = 0; i < 5; i++) {
			tasks.add(new ParallelizedTask(i, repetitions));
		}
		Result res = new Result(repetitions);
		for (ParallelizedTask pt : tasks) {
			res.absorb(pt.getResult());
		}
		System.out.println(res);
		long t1 = System.nanoTime();
		System.out.println("Took " + (t1 - t0) / 1000000 + " to calculate");
	}

}
