package com.reflexit.magiccards.ui.views.editions;

import java.util.Locale;

import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardField;

public enum EditionField implements ICardField {
	NAME,
	DATE("release"),
	TYPE,
	FORMAT("legalityMap")
	// end
	;
	private final String tag;

	EditionField(String javaField) {
		tag = null;
	}

	EditionField() {
		tag = name().toLowerCase(Locale.ENGLISH);
	}

	@Override
	public Object aggregateValueOf(ICard card) {
		return null;
	}



	public boolean isTransient() {
		return false;
	}

	public String getLabel() {
		String name = name();
		return name.charAt(0) + name.substring(1).toLowerCase(Locale.ENGLISH);
	}

	@Override
	public String getTag() {
		return tag;
	}
}
