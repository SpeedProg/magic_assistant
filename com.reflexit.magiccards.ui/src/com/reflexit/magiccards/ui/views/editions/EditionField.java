package com.reflexit.magiccards.ui.views.editions;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;

import com.reflexit.magiccards.core.model.Editions.Edition;
import com.reflexit.magiccards.core.model.ICardField;

public enum EditionField implements ICardField {
	NAME,
	DATE("release"),
	TYPE,
	FORMAT
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
			field = Edition.class.getDeclaredField(javaField);
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

	public String getGroupLabel() {
		String name = name();
		return name.charAt(0) + name.substring(1).toLowerCase(Locale.ENGLISH);
	}
}
