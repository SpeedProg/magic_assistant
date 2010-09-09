package com.reflexit.magiccards.core.model;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

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
		String javaField = name().toLowerCase();
		try {
			field = MagicCard.class.getDeclaredField(javaField);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public Class getType() {
		return field == null ? String.class : field.getClass();
	}

	public boolean isTransient() {
		return field == null ? true : Modifier.isTransient(field.getModifiers());
	}
}
