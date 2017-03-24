package com.meissereconomics.trade.util;

import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;

public class Table {

	private String[] labels;
	private Set<String> types;
	private TableKey current;
	private InstantiatingHashmap<TableKey, TableValue> entries;
	
	public Table(String... labels){
		this.labels = labels;
		this.types = new TreeSet<>();
		this.current = new TableKey(labels);
		this.entries = new InstantiatingHashmap<TableKey, TableValue>() {
			
			@Override
			protected TableValue createValue(TableKey key) {
				return new TableValue();
			}
		};
	}
	
	public void include(String type, double value){
		assert !Double.isNaN(value);
		this.types.add(type);
		this.entries.obtain(current).include(type, value);
	}

	public void setCurrent(String what, String current) {
		this.current = this.current.derive(what, current);
	}

	public void printAll() {
		String row = Formatter.toTabs((Object[])labels);
		for (String type: types){
			row += "\t" + type + "\tVariance of " + type; 
		}
		System.out.println(row);
		entries.forEach(new BiConsumer<TableKey, TableValue>() {

			@Override
			public void accept(TableKey t, TableValue u) {
				String row = t.toString();
				for (String value: types){
					row += "\t" + u.getValue(value);
				}
				System.out.println(row);
			}
			
		});
	}

}
