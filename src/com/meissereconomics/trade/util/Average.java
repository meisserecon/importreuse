// Created on Jun 23, 2015 by Luzius Meisser

package com.meissereconomics.trade.util;

import java.util.Collection;

public class Average implements Cloneable {
	
	private double weight;
	private double sum, squaredSum;
	private double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
	
	public Average(){
	}
	
	public Average(Collection<Double> initial){
		for (Double d: initial){
			add(d);
		}
	}
	
	public void add(Average other){
		this.weight += other.weight;
		this.sum += other.sum;
		this.squaredSum += other.squaredSum;
		this.min = Math.min(min, other.min);
		this.max = Math.max(max, other.max);
	}
	
	public void add(double price) {
		add(1.0, price);
	}
	
	public void add(double weight, double price) {
		this.weight += weight;
		this.sum += weight * price;
		this.squaredSum += weight * price * price;
		this.min = Math.min(min, price);
		this.max = Math.max(max, price);
	}
	
	public double getTotWeight(){
		return weight;
	}
	
	public boolean hasValue(){
		return weight > 0.0;
	}
	
	public double getAverage() {
		return sum / weight;
	}
	
	public double getVariance(){
		double avg = getAverage();
		return squaredSum / weight - avg * avg;
	}
	
	public double getMin(){
		return min;
	}
	
	public double getMax(){
		return max;
	}
	
	@Override
	public Average clone(){
		try {
			return (Average) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new java.lang.RuntimeException(e);
		}
	}

	@Override
	public String toString(){
		return "Avg: " + getAverage();
	}
	
	public String toFullString(){
		return getAverage() + " (" + getMin() + ", " + getMax() + ")";
	}
	
}
