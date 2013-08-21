package com.reflexit.magiccards.core.model;

public enum Legality {
	NOT_LEGAL,
	BANNED,
	RESTRICTED,
	LEGAL;
	public static Legality fromLabel(String x) {
		if (x.equals("Legal"))
			return LEGAL;
		if (x.equals("Restricted"))
			return RESTRICTED;
		if (x.equals("Not Legal"))
			return NOT_LEGAL;
		if (x.equals("Banned"))
			return BANNED;
		throw new IllegalArgumentException();
	}
}
