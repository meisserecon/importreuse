package com.meissereconomics.trade.util;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.util.ResizableDoubleArray;

public class TableValue {
	
	private InstantiatingHashmap<String, ResizableDoubleArray> array;
	
	public TableValue(){
		this.array = new InstantiatingHashmap<String, ResizableDoubleArray>(){

			@Override
			protected ResizableDoubleArray createValue(String key) {
				return new ResizableDoubleArray();
			}
			
		};
	}

	public void include(String type, double value) {
		this.array.obtain(type).addElement(value);
	}
	
	@Override
	public String toString(){
		return array.toString();
	}

	public String getValue(String key, boolean includeVariance) {
		double[] values = array.get(key).getElements();
		String mean = Double.toString(new Mean().evaluate(values));
		if (includeVariance){
			mean += "\t" + new Variance().evaluate(values);
		}
		return mean;
	}

}
