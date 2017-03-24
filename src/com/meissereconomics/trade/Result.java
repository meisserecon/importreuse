package com.meissereconomics.trade;

public class Result {
	
	private int pos = 0;
	private int weight = 0;
	private double[] values;
	
	public Result(int size){
		this.values = new double[size];
	}

	public void append(double reuse) {
		values[pos++] = reuse;
		if (pos == values.length){
			weight = 1;
		}
	}

	public void absorb(Result res) {
		int totWeight = weight + res.weight;
		for (int i=0; i<res.values.length; i++){
			this.values[i] = this.values[i] * weight + res.values[i] * res.weight / totWeight;
		}
		this.weight += res.weight;
	}
	
	public double[] getResult(){
		return values;
	}
	
	@Override
	public String toString(){
		String table = "Entry\tValue";
		for (int i=0;i<values.length; i++){
			table += "\n" + i + "\t" + values[i];
		}
		return table;
	}
	
}
