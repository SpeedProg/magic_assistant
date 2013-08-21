package com.reflexit.magiccards.core.model;

import java.util.HashMap;
import java.util.Map;

public enum Legality {
	NOT_LEGAL("Not Legal", "-"),
	BANNED("Banned", "!"),
	RESTRICTED("Restricted", "1"),
	LEGAL("Legal", "+");
	private String label;
	private String ext;

	Legality(String label, String ext) {
		this.label = label;
		this.ext = ext;
	}

	public static Legality fromLabel(String x) {
		for (Legality leg : values()) {
			if (leg.label.equals(x))
				return leg;
		}
		throw new IllegalArgumentException();
	}

	public static Legality fromExt(String x) {
		for (Legality leg : values()) {
			if (leg.ext.equals(x))
				return leg;
		}
		throw new IllegalArgumentException();
	}

	public final static String formats[] = { "Standard", "Extended", "Modern", "Legacy", "Vintage", "Classic", "Freeform" };

	public static String external(Map<String, Legality> map) {
		String res = "";
		for (String format : formats) {
			Legality leg = map.get(format);
			if (leg == null)
				leg = NOT_LEGAL;
			res += format.charAt(0) + leg.ext + " ";
		}
		return res.trim();
	}

	public static Map<String, Legality> internal(String value) {
		Map<String, Legality> map = new HashMap<String, Legality>();
		String vs[] = value.split(" ");
		for (int i = 0; i < vs.length; i++) {
			String string = vs[i];
			map.put(formats[i], fromExt(string.substring(1)));
		}
		return map;
	}

	public String getLabel() {
		return label;
	}
}
