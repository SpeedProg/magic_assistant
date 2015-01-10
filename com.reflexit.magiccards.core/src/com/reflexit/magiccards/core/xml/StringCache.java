package com.reflexit.magiccards.core.xml;

import java.util.HashMap;

public class StringCache {
	private static HashMap<String, String> stringCache = new HashMap<String, String>();

	public static String intern(String x) {
		if (x == null)
			return null;
		String res = stringCache.get(x);
		if (res == null) {
			res = x.intern();
			stringCache.put(res, res);
		}
		return res;
	}

	public static void clear() {
		stringCache.clear();
	}
}
