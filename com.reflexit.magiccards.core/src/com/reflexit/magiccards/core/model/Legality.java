package com.reflexit.magiccards.core.model;

public enum Legality {
	NOT_LEGAL("Not Legal", "-"),
	BANNED("Banned", "!"),
	RESTRICTED("Restricted", "1"),
	LEGAL("Legal", "+"),
	UNKNOWN("Unknown", "?");
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
		throw new IllegalArgumentException("Unknown legality label: " + x);
	}

	public static Legality fromExt(String x) {
		for (Legality leg : values()) {
			if (leg.ext.equals(x))
				return leg;
		}
		if (x.equals("*") || x.isEmpty())
			return UNKNOWN;
		throw new IllegalArgumentException("Unknown legality extension: " + x);
	}

	public static boolean isExt(String x) {
		for (Legality leg : values()) {
			if (leg.ext.equals(x))
				return true;
		}
		return false;
	}

	public String getLabel() {
		return label;
	}

	public String getExt() {
		return ext;
	}
}
