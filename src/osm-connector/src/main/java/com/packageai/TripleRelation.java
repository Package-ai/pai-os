/**
 * Copyright (c) 2018 Package.ai. All Rights Reserved.
 */
package com.packageai;

import java.util.*;

/**
 *
 */
public class TripleRelation <K1, K2,V> {

	private Map<K1, Map<K2, V>> map;

	public TripleRelation(){
		this.map = new HashMap<>();
	}

	public TripleRelation(TripleRelation<K1, K2,V> toClone){
		this.map = new HashMap<>();
		for (K1 k1 : toClone.allFirstKeys()){
			for (K2 k2 : toClone.secondKeys(k1)){
				this.setValue(k1, k2, toClone.value(k1, k2));
			}
		}
	}

	public void setMap(Map<K1, Map<K2, V>> map) {
		this.map = map;
	}

	public Map<K1, Map<K2, V>> getMap(){
		return map;
	}

	public void setValue(K1 key1, K2 key2, V value){
		Map<K2, V> secondsToValues = this.map.get(key1);
		if (secondsToValues == null){
			secondsToValues = new HashMap<>();
			this.map.put(key1, secondsToValues);
		}
		secondsToValues.put(key2, value);
	}

	public Set<K1> allFirstKeys(){
		return Collections.unmodifiableSet(this.map.keySet());
	}

	public Set<K2> secondKeys(K1 firstKey){
		Map<K2, V> secondKeysToValues = this.map.get(firstKey);
		if (secondKeysToValues == null){
			return Collections.unmodifiableSet(Collections.emptySet());
		}
		return Collections.unmodifiableSet(secondKeysToValues.keySet());
	}

	public V value(K1 key1, K2 key2) {
		Map<K2, V> secondKeysToValues = this.map.get(key1);
		if (secondKeysToValues == null) {
			return null;
		}
		return secondKeysToValues.get(key2);
	}

	public boolean containsFirstKey(K1 key){
		return this.map.containsKey(key);
	}

	public boolean containsRelation(K1 key1, K2 key2){
		return value(key1, key2) != null;
	}

	public Map<K2, V> secondKeysAndValues(K1 key){
		if (!containsFirstKey(key)){
			return Collections.unmodifiableMap(Collections.emptyMap());
		}
		return Collections.unmodifiableMap(this.map.get(key));
	}

	public Collection<V> allValues(){
		List<V> result = new ArrayList<>();
		Set<K1> allFirstKeys = allFirstKeys();
		for (K1 k1 : allFirstKeys){
			result.addAll(secondKeysAndValues(k1).values());
		}
		return Collections.unmodifiableCollection(result);
	}

}
