package com.meissereconomics.trade.util;

import java.util.Collection;

public class Formatter {

	public static String toTabs(double... c) {
		return toTabsDouble(c);
	}

	public static String toTabsDouble(double... c) {
		String s = "";
		for (double o : c) {
			if (s.isEmpty()) {
				s = Double.toString(o);
			} else {
				s += "\t" + o;
			}
		}
		return s;
	}

	public static String toTabs(Object... c) {
		String s = "";
		for (Object o : c) {
			if (s.isEmpty()) {
				s = o.toString();
			} else {
				s += "\t" + o.toString();
			}
		}
		return s;
	}

	public static <T> String toTabs(Collection<T> c) {
		String s = "";
		for (T o : c) {
			if (s.isEmpty()) {
				s = o.toString();
			} else {
				s += "\t" + o.toString();
			}
		}
		return s;
	}

	public static String toString(double value) {
		return Double.toString(value);
	}

}
