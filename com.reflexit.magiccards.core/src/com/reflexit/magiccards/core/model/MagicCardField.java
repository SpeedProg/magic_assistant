package com.reflexit.magiccards.core.model;

public enum MagicCardField implements ICardField {
	ID(Integer.class),
	NAME,
	COST,
	TYPE,
	POWER,
	TOUGHNESS,
	ORACLE,
	SET,
	RARITY,
	CTYPE(String.class, true),
	CMC(Integer.class, true),
	DBPRICE(Float.class),
	LANG,
	EDITION_ABBR(String.class, true),
	COMMUNITYRATING(Float.class),
	ARTIST(String.class),
	COLLNUM(String.class), // collector number i.e. 5/234
	RULINGS(String.class, true), ;
	// fields
	private final Class type;
	// transient field is not stored in xml
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
