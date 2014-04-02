package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Locale;

public enum MagicCardField implements ICardField {
	ID,
	NAME,
	COST,
	TYPE,
	POWER,
	TOUGHNESS,
	ORACLE("oracleText"),
	SET("edition"),
	RARITY,
	CTYPE(null),
	CMC(null),
	DBPRICE(),
	LANG,
	EDITION_ABBR(null),
	RATING,
	ARTIST,
	COLLNUM("num"), // collector number i.e. 5/234
	RULINGS,
	TEXT,
	ENID("enId"),
	PROPERTIES,
	FLIPID(null),
	PART(null),
	OTHER_PART(null),
	SET_BLOCK(null), // block of the set
	SET_CORE(null), // type of the set (Core, Expantions, etc)
	UNIQUE_COUNT(null) {
		@Override
		public Object valueOf(ICard card) {
			return card.accept(FieldUniqueAggregator.getInstance(), null);
		}
	}, // count of unique cards (usually only make sense for group)
	SIDE(null), // for multi sides/duble/flip card represent version of card (0 or 1)
	IMAGE_URL(null), // for non gatherer cards
	LEGALITY(null),
	COLOR(null),
	// end of magic base fields
	COUNT(true),
	PRICE(true),
	COMMENT(true),
	LOCATION(true),
	CUSTOM(true),
	OWNERSHIP(true),
	FORTRADECOUNT("forTrade", true),
	SPECIAL(true), // like foil, premium, mint, played, online etc
	SIDEBOARD(null, true),
	OWN_COUNT(null, true) {
		@Override
		public Object valueOf(ICard card) {
			return card.accept(FieldOwnCountAggregator.getInstance(), null);
		}
	}, // count of own card (normal count counts own and virtual)
	OWN_UNIQUE(null, true) {
		@Override
		public Object valueOf(ICard card) {
			return card.accept(FieldOwnUniqueAggregator.getInstance(), null);
		}
	}, // count of own unique cards (only applies to groups usually)
	ERROR(null, true), // error field for import
	// end of fields
	;
	private final String tag;
	private final boolean phys;

	MagicCardField() {
		this(false);
	}

	MagicCardField(String javaField) {
		this(javaField, false);
	}

	MagicCardField(boolean physical) {
		tag = name().toLowerCase(Locale.ENGLISH);
		this.phys = physical;
	}

	MagicCardField(String javaField, boolean physical) {
		tag = javaField;
		phys = physical;
	}

	public boolean isTransient() {
		return tag == null;
	}

	public static ICardField[] allFields() {
		MagicCardField[] values = MagicCardField.values();
		return values;
	}

	public static ICardField[] allNonTransientFields(boolean phys) {
		MagicCardField[] values = MagicCardField.values();
		ArrayList<ICardField> res = new ArrayList<ICardField>();
		for (MagicCardField f : values) {
			if (!f.isTransient()) {
				if (phys || !f.phys)
					res.add(f);
			}
		}
		return res.toArray(new ICardField[res.size()]);
	}

	public String getTag() {
		return tag;
	}

	public Object valueOf(ICard card) {
		return card.getObjectByField(this);
	}

	public static ICardField fieldByName(String field) {
		if (field == null || field.length() == 0)
			return null;
		try {
			MagicCardField f = valueOf(field);
			if (f != null)
				return f;
		} catch (Exception e) {
			// ignore
		}
		// aliases
		if (field.equals("EDITION"))
			return SET;
		if (field.equals("QTY"))
			return COUNT;
		return null;
	}

	public static ICardField[] toFields(String line, String sep) {
		String split[] = line.split(sep);
		ICardField res[] = new ICardField[split.length];
		for (int i = 0; i < split.length; i++) {
			String string = split[i];
			ICardField field = fieldByName(string);
			res[i] = field;
		}
		return res;
	}

	public String getGroupLabel() {
		String name = name();
		name = name.charAt(0) + name.substring(1).toLowerCase(Locale.ENGLISH);
		name = name.replace('_', ' ');
		return name;
	}
}
