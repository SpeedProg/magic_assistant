package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class Colors implements ISearchableProperty {
	static Colors instance = new Colors();
	private LinkedHashMap names;
	private HashMap codes;

	private Colors() {
		this.names = new LinkedHashMap();
		this.codes = new HashMap();
		add("Black", "B");
		add("White", "W");
		add("Red", "R");
		add("Blue", "U");
		add("Green", "G");
		add("Multi-Color", null);
		add("Colorless", null);
	};

	private void add(String string, String code) {
		String id = getPrefConstant(string);
		this.names.put(id, string);
		this.codes.put(id, code);
	}

	public static String getColorName(String cost) {
		if (cost.length() == 0)
			return "No Cost";
		StringBuffer color = new StringBuffer();
		addColor("B", "Black", cost, color);
		addColor("U", "Blue", cost, color);
		addColor("G", "Green", cost, color);
		addColor("R", "Red", cost, color);
		addColor("W", "White", cost, color);
		String res = color.toString();
		if (res.length() == 0)
			return "Colorless";
		return res;
	}

	/**
	 
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.ISearchableProperty#getIdPrefix()
	 */
	public String getIdPrefix() {
		return "colors";
	}

	public static Colors getInstance() {
		return instance;
	}

	public Collection getNames() {
		return new ArrayList(this.names.values());
	}

	public Collection getIds() {
		return new ArrayList(this.names.keySet());
	}

	public String getPrefConstant(String name) {
		return FilterHelper.getPrefConstant(getIdPrefix(), name);
	}

	public String getNameById(String id) {
		return (String) this.names.get(id);
	}

	public String getEncodeByName(String r) {
		return (String) this.codes.get(getPrefConstant(r));
	}

	public String getColorType(String cost) {
		if (cost == null || cost.length() == 0)
			return "land";
		String[] manas = manasplit(cost);
		Map colors = new HashMap();
		for (String x : manas) {
			if (x.equals("X") || x.equals("Y") || x.equals("Z")) {
				continue;
			}
			if (x.matches("\\d+")) {
				continue;
			}
			if (x.contains("/"))
				return "hybrid";
			colors.put(x, x);
		}
		int diff = 0;
		for (Iterator iterator = colors.keySet().iterator(); iterator.hasNext();) {
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
			if (x.matches("\\d+")) {
				res += Integer.parseInt(x);
				continue;
			}
			res++;
		}
		return res;
	}

	private String[] manasplit(String cost) {
		if (cost.contains(","))
			return new String[] { "1000000" };
		String res = cost.replaceAll("\\{", "");
		res = res.replaceAll("\\}$", "");
		String manas[] = res.split("\\}");
		return manas;
	}
}
