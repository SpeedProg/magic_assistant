package com.reflexit.magiccards.core.model;

import java.util.ArrayList;

/**
 * Fields for actual player card
 */
public enum MagicCardFieldPhysical implements ICardField {
	COUNT(Integer.class),
	PRICE(Float.class),
	COMMENT,
	LOCATION,
	CUSTOM,
	OWNERSHIP(Boolean.class),
	FORTRADECOUNT(Integer.class),
	SPECIAL, // like foil, premium, mint, played, online etc
	// end of fields
	;
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

	public static ICardField[] allFields() {
		MagicCardField[] values = MagicCardField.values();
		MagicCardFieldPhysical[] values2 = values();
		ICardField[] res = new ICardField[values.length + values2.length];
		System.arraycopy(values, 0, res, 0, values.length);
		System.arraycopy(values2, 0, res, values.length, values2.length);
		return res;
	}

	public static ICardField[] allNonTransientFields() {
		MagicCardField[] values = MagicCardField.values();
		MagicCardFieldPhysical[] values2 = values();
		ArrayList<ICardField> res = new ArrayList<ICardField>();
		for (MagicCardField f : values) {
			if (!f.isTransient())
				res.add(f);
		}
		for (MagicCardFieldPhysical f : values2) {
			if (!f.isTransient())
				res.add(f);
		}
		return res.toArray(new ICardField[res.size()]);
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
		// aliases
		if (field.equals("EDITION"))
			return MagicCardField.SET;
		if (field.equals("QTY"))
			return MagicCardFieldPhysical.COUNT;
		return null;
	}
}
