package com.meissereconomics.trade.graph;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.DoubleConsumer;
import java.util.function.ObjDoubleConsumer;

import com.meissereconomics.trade.util.Formatter;

import net.openhft.koloboke.collect.map.hash.HashObjDoubleMap;
import net.openhft.koloboke.collect.map.hash.HashObjDoubleMaps;

public class Edges {
	
	private HashObjDoubleMap<Node> map;
	private int modifications, sumTime;
	private double sum;
	
	public Edges(){
		this.modifications = 0;
		this.sumTime = 0;
		this.sum = 0.0;
		this.map = HashObjDoubleMaps.newMutableMap();
	}


	public void forEach(ObjDoubleConsumer<Node> consumer) {
		map.forEach(consumer);
	}


	public double getDouble(Node consumption) {
		return map.getDouble(consumption);
	}


	public boolean containsKey(Node node) {
		return map.containsKey(node);
	}


	public void shrink() {
		map.shrink();
	}


	public double removeAsDouble(Node other) {
		this.modifications++;
		return map.removeAsDouble(other);
	}


	public void put(Node node, double sum2) {
		this.modifications++;
		map.put(node, sum2);
	}


	public Set<Node> keySet() {
		return Collections.unmodifiableSet(map.keySet());
	}


	public Iterator<Entry<Node, Double>> entryIterator() {
		return new Iterator<Map.Entry<Node,Double>>() {
			
			private Iterator<Map.Entry<Node,Double>> wrapped = map.entrySet().iterator();
			
			@Override
			public Entry<Node, Double> next() {
				return wrapped.next();
			}
			
			@Override
			public boolean hasNext() {
				return wrapped.hasNext();
			}
			
			public void remove(){
				Edges.this.modifications++;
				wrapped.remove();
			}
		};
	}

	public double getSum() {
		if (sumTime != modifications){
			sum = 0.0;
			map.values().forEach(new DoubleConsumer() {
				
				@Override
				public void accept(double value) {
					sum += value;
				}
			});
			sumTime = modifications;
		}
		return sum;
	}
	
	@Override
	public String toString(){
		final String[] msg = new String[]{"Edges: "};
		final double sum = getSum();
		forEach(new ObjDoubleConsumer<Node>() {
			
			@Override
			public void accept(Node t, double value) {
				if (value > sum / 50){
					msg[0] += t.getIndustry() + " " + Formatter.toString(value) + ", ";
				}
			}
		});
		return msg[0] + "...";
	}

}
