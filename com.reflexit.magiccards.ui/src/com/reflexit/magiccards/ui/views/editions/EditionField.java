package com.reflexit.magiccards.ui.views.editions;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.reflexit.magiccards.core.model.Editions.Edition;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCard;

public enum EditionField implements ICardField {
	NAME,
	DATE("release"),
	TYPE
	// end
	;
	private final Field field;

	EditionField(String javaField) {
		if (javaField != null)
			try {
				field = Edition.class.getDeclaredField(javaField);
			} catch (Exception e) {
				throw new IllegalArgumentException(e);
			}
		else
			field = null;
	}

	EditionField() {
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
