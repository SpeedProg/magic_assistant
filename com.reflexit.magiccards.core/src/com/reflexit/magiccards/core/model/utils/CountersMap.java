package com.reflexit.magiccards.core.model.utils;

import java.util.HashMap;

public class CountersMap extends HashMap<String, Integer> {
	private static final Integer ZERO = Integer.valueOf(0);

	public Integer inc(String name, int count) {
		if (containsKey(name)) {
			return put(name, get(name) + count);
		} else {
			return put(name, count);
		}
	}

	public Integer inc(String name) {
		return inc(name, 1);
	}

	public Integer get(String key) {
		Integer x = super.get(key);
		if (x == null)
			return ZERO;
		return x;
	}

	public int max() {
		int max = 0;
		for (Integer v : values()) {
			if (v > max)
				max = v;
		}
		return max;
	}

	public String maxKey() {
		String rkey = null;
		int max = 0;
		for (String key : keySet()) {
			int v = get(key);
			if (v > max) {
				max = v;
				rkey = key;
			}
		}
		return rkey;
	}
}
