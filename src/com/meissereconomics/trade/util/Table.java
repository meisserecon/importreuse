package com.meissereconomics.trade.util;

import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;

public class Table {

	private String[] labels;
	private Set<String> types;
	private TableKey current;
	private boolean printedLabels;
	private InstantiatingHashmap<TableKey, TableValue> entries;

	public Table(String... labels) {
		this.labels = labels;
		this.types = new TreeSet<>();
		this.printedLabels = false;
		this.current = new TableKey(labels);
		this.entries = new InstantiatingHashmap<TableKey, TableValue>() {

			@Override
			protected TableValue createValue(TableKey key) {
				return new TableValue();
			}
		};
	}

	public void include(String type, double value) {
		assert !Double.isNaN(value);
		this.types.add(type);
		this.entries.obtain(current).include(type, value);
	}

	public void setCurrent(String what, String current) {
		this.current = this.current.derive(what, current);
	}

	public void printAll() {
		printAll(true);
	}
	
	public void printAndFlush(boolean includeVariance){
		if (!printedLabels){
			printLabels(includeVariance);
			printedLabels=true;
		}
		printRows(includeVariance);
		entries.clear();
	}

	public void printAll(boolean includeVariance) {
		printLabels(includeVariance);
		printRows(includeVariance);
	}

	protected void printLabels(boolean includeVariance) {
		String row = Formatter.toTabs((Object[]) labels);
		for (String type : types) {
			row += "\t" + type;
			if (includeVariance) {
				row += "\tVariance of " + type;
			}
		}
		System.out.println(row);
	}

	protected void printRows(boolean includeVariance) {
		entries.forEach(new BiConsumer<TableKey, TableValue>() {

			@Override
			public void accept(TableKey t, TableValue u) {
				String row = t.toString();
				for (String value : types) {
					row += "\t" + u.getValue(value, includeVariance);
				}
				System.out.println(row);
			}

		});
	}

}
