package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class Colors implements ISearchableProperty {
	static Colors instance = new Colors();
	private LinkedHashMap<String, String> names;
	private HashMap<String, String> codes;

	private Colors() {
		this.names = new LinkedHashMap<String, String>();
		this.codes = new HashMap<String, String>();
		add("White", "W");
		add("Blue", "U");
		add("Black", "B");
		add("Red", "R");
		add("Green", "G");
		add("Colorless", "1");
	}

	private void add(String string, String code) {
		String id = getPrefConstant(string);
		this.names.put(id, string);
		this.codes.put(id, code);
	}

	public static String getColorName(String cost) {
		if (cost == null)
			return "Unknown";
		if (cost.length() == 0)
			return "No Cost";
		StringBuffer color = new StringBuffer();
		addColor("W", "White", cost, color);
		addColor("U", "Blue", cost, color);
		addColor("B", "Black", cost, color);
		addColor("R", "Red", cost, color);
		addColor("G", "Green", cost, color);
		String res = color.toString();
		if (res.length() == 0)
			return "Colorless";
		return res;
	}

	public static int getColorSort(String cost) {
		if (cost == null)
			return 0;
		if (cost.length() == 0)
			return 0;
		int xx = 0;
		char c[] = { 'W', 'U', 'B', 'R', 'G' };
		int times = 0;
		for (int i = 0; i < c.length; i++) {
			char cw = c[i];
			xx *= 10;
			if (cost.indexOf(cw) >= 0) {
				times++;
				xx += 5 - i;
			}
		}
		if (xx == 0)
			return 1;
		if (times == 1)
			xx *= 100000;
		return -xx;
	}

	/**
	 *
	 * @param abbr
	 * @param name
	 * @param cost
	 * @param buf
	 */
	private static void addColor(String abbr, String name, String cost, StringBuffer buf) {
		if (cost.indexOf(abbr) >= 0) {
			if (buf.length() > 0) {
				buf.append('-');
			}
			buf.append(name);
		}
	}

	@Override
	public String getIdPrefix() {
		return getFilterField().toString();
	}

	@Override
	public FilterField getFilterField() {
		return FilterField.COLOR;
	}

	public static Colors getInstance() {
		return instance;
	}

	public Collection<String> getNames() {
		return new ArrayList<String>(this.names.values());
	}

	@Override
	public Collection<String> getIds() {
		return new ArrayList<String>(this.names.keySet());
	}

	public String getPrefConstant(String name) {
		return FilterField.getPrefConstant(getIdPrefix(), name);
	}

	@Override
	public String getNameById(String id) {
		return this.names.get(id);
	}

	public String getEncodeByName(String r) {
		return this.codes.get(getPrefConstant(r));
	}

	public static String getColorType(String cost) {
		if (cost == null || cost.length() == 0)
			return "land";
		String[] manas = manasplit(cost);
		Map<String, String> colors = new HashMap<String, String>();
		for (String x : manas) {
			char firstChar = x.charAt(0);
			if (firstChar == 'X' || firstChar == 'Y' || firstChar == 'Z')
				continue;
			if (x.contains("/"))
				return "hybrid";
			if (Character.isDigit(firstChar))
				continue;
			colors.put(x, x);
		}
		int diff = 0;
		for (Iterator<String> iterator = colors.keySet().iterator(); iterator.hasNext();) {
			iterator.next();
			diff++;
		}
		if (diff == 0)
			return "colorless";
		if (diff == 1)
			return "mono";
		return "multi";
	}

	public int getConvertedManaCost(String cost) {
		if (cost == null || cost.length() == 0)
			return 0;
		String[] manas = manasplit(cost);
		int res = 0;
		for (String x : manas) {
			if (x.equals("X") || x.equals("Y") || x.equals("Z")) {
				res += 0;
				continue;
			}
			if (Character.isDigit(x.charAt(0))) {
				try {
					res += Integer.parseInt(x);
					continue;
				} catch (Exception e) {
					// ignore
				}
			}
			res++;
		}
		return res;
	}

	public static String[] manasplit(String cost) {
		if (cost.contains(","))
			return new String[] { "1000000" };
		int k = 0;
		for (int i = 0; i < cost.length(); i++) {
			if (cost.charAt(i) == '{') {
				k++;
			}
		}
		String manas[] = new String[k];
		int ik = 0;
		for (int i = 0; i < cost.length(); i++) {
			if (cost.charAt(i) == '{') {
				int l = cost.indexOf('}', i);
				manas[ik++] = cost.substring(i + 1, l);
				i = l;
			}
		}
		return manas;
	}
}
