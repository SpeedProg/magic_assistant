package com.reflexit.magiccards.core.model;

public enum MagicCardFieldPhysical implements ICardField {
	COUNT(Integer.class),
	PRICE(Float.class),
	COMMENT,
	LOCATION,
	CUSTOM,
	OWNERSHIP(Boolean.class), ;
	// fields
	private final Class type;
	private final boolean transientField;

	MagicCardFieldPhysical(Class type, boolean trans) {
		this.type = type;
		this.transientField = trans;
	}

	MagicCardFieldPhysical(Class type) {
		this(type, false);
	}

	MagicCardFieldPhysical() {
		this(String.class);
	}

	public Class getType() {
		return type;
	}

	public boolean isTransient() {
		return transientField;
	}

	public static ICardField fieldByName(String field) {
		try {
			MagicCardFieldPhysical p = valueOf(field);
			if (p != null)
				return p;
		} catch (Exception e) {
			// ignore
		}
		try {
			MagicCardField f = MagicCardField.valueOf(field);
			if (f != null)
				return f;
		} catch (Exception e) {
			// ignore
		}
		return null;
	}
}
