package com.reflexit.magiccards.core.model;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
	CTYPE("colorType"),
	CMC,
	DBPRICE,
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
	UNIQUE_COUNT(null), // count of unique cards (usually only make sense for group)
	SIDE(null), // for multi sides/duble/flip card represent version of card (0 or 1)
	IMAGE_URL(null), // for non gatherer cards
	LEGALITY(null),
	COLOR(null),
	// end
	;
	private final Field field;

	MagicCardField(String javaField) {
		if (javaField != null)
			try {
				field = MagicCard.class.getDeclaredField(javaField);
			} catch (Exception e) {
				throw new IllegalArgumentException(e);
			}
		else
			field = null;
	}

	MagicCardField() {
		String javaField = name().toLowerCase(Locale.ENGLISH);
		try {
			field = MagicCard.class.getDeclaredField(javaField);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public Class getType() {
		return field == null ? String.class : field.getType();
	}

	public boolean isTransient() {
		return field == null ? true : Modifier.isTransient(field.getModifiers());
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
		return res.toArray(new ICardField[res.size()]);
	}

	public Field getJavaField() {
		return field;
	}

	public String getGroupLabel() {
		String name = name();
		return name.charAt(0) + name.substring(1).toLowerCase(Locale.ENGLISH);
	}
}
