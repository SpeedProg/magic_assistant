package com.reflexit.magiccards.core.model.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class MRUCache<K, V> extends LinkedHashMap<K, V> {
	private int max;

	public MRUCache() {
		this(500);
	}

	public MRUCache(int max) {
		super(100, 0.75f, true);
		this.max = max;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry eldest) {
		return size() > max;
	}
}
