package com.meissereconomics.seminar.util;

import java.util.Arrays;

public class TableKey {
	
	private final String[] labels;
	private final String[] keys;
	
	public TableKey(String[] labels){
		this.labels = labels;
		this.keys = new String[labels.length];
	}

	public TableKey(String[] labels, String[] keys) {
		this.labels = labels;
		this.keys = keys;
	}

	public TableKey derive(String what, String current) {
		String[] keys = new String[this.keys.length];
		for (int i=0; i<keys.length; i++){
			if (labels[i].equals(what)){
				keys[i] = current;
			} else {
				keys[i] = this.keys[i];
			}
		}
		return new TableKey(labels, keys);
	}

	@Override
	public boolean equals(Object o){
		TableKey ok = (TableKey)o;
		return Arrays.equals(keys, ok.keys);
	}
	
	@Override
	public int hashCode(){
		return Arrays.hashCode(keys);
	}
	
	public String toString(){
		return Formatter.toTabs(keys);
	}
	
}
