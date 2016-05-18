package com.meissereconomics.seminar.util;

import java.util.Collection;

public class Formatter {
	
	public static String toTabs(double... c){
		String s = "";
		for (double o: c){
			if (s.isEmpty()){
				s = Double.toString(o);
			} else {
				s += "\t" + o;
			}
		}
		return s;
	}
	
	public static String toTabs(Object... c){
		String s = "";
		for (Object o: c){
			if (s.isEmpty()){
				s = o.toString();
			} else {
				s += "\t" + o.toString();
			}
		}
		return s;
	}
	
	public static <T> String toTabs(Collection<T> c){
		String s = "";
		for (T o: c){
			if (s.isEmpty()){
				s = o.toString();
			} else {
				s += "\t" + o.toString();
			}
		}
		return s;
	}
	
	public static String getFilename(int year) {
		if (year <= 1999) {
			return "data/wiot" + (year - 1900) + "_row_apr12.csv";
		} else if (year <= 2007) {
			return "data/wiot0" + (year - 2000) + "_row_apr12.csv";
		} else if (year <= 2009) {
			return "data/wiot0" + (year - 2000) + "_row_sep12.csv";
		} else {
			return "data/wiot" + (year - 2000) + "_row_sep12.csv";
		}
	}

}
