package com.reflexit.magiccards.core.model;


public enum MagicCardField implements ICardField {
	ID(Integer.class),
	NAME,
	COST,
	TYPE,
	POWER,
	TOUGHNESS,
	ORACLE,
	EDITION,
	RARITY,
	CTYPE(String.class, true),
	CMC(Integer.class, true);
	// fields
	private final Class type;
	private final boolean transientField;

	MagicCardField(Class type, boolean trans) {
		this.type = type;
		this.transientField = trans;
	}

	MagicCardField(Class type) {
		this(type, false);
	}

	MagicCardField() {
		this(String.class);
	}

	public Class getType() {
		return type;
	}

	public boolean isTransient() {
		return transientField;
	}
}
