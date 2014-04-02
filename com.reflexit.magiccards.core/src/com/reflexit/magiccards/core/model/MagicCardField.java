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
	// end
	;
	private final String tag;

	MagicCardField(String javaField) {
		tag = javaField;
	}

	public Object valueOf(ICard card) {
		return card.getObjectByField(this);
	}

	MagicCardField() {
		tag = name().toLowerCase(Locale.ENGLISH);
	}

	public boolean isTransient() {
		return tag == null;
	}

	public static ICardField[] allFields() {
		MagicCardField[] values = MagicCardField.values();
		return values;
	}

	public static ICardField[] allNonTransientFields() {
		MagicCardField[] values = MagicCardField.values();
		ArrayList<ICardField> res = new ArrayList<ICardField>();
		for (MagicCardField f : values) {
			if (!f.isTransient())
				res.add(f);
		}
		// res.add(DBPRICE);
		return res.toArray(new ICardField[res.size()]);
	}

	public String getGroupLabel() {
		String name = name();
		return name.charAt(0) + name.substring(1).toLowerCase(Locale.ENGLISH);
	}

	public String getTag() {
		return tag;
	}
}
