package com.meissereconomics.seminar.util;
import java.util.HashMap;

public abstract class InstantiatingHashmap<K, V> extends HashMap<K, V> {
	
	public V obtain(K key) {
		V value = super.get(key);
		if (value == null){
			value = createValue(key);
			super.put(key, value);
		}
		return value;
	}

	protected abstract V createValue(K key);

}
