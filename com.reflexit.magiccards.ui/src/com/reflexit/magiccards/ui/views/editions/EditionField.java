package com.reflexit.magiccards.ui.views.editions;

import java.util.Locale;

import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;

public enum EditionField implements ICardField {
	NAME,
	DATE,
	TYPE,
	FORMAT,
	BLOCK,
	ALIASES,
	// end
	;
	EditionField() {
	}

	@Override
	public Object aggregateValueOf(ICard card) {
		return null;
	}

	@Override
	public boolean isTransient() {
		return false;
	}

	@Override
	public String getLabel() {
		String name = name();
		name = name.charAt(0) + name.substring(1).toLowerCase(Locale.ENGLISH);
		name = name.replace('_', ' ');
		return name;
	}
}
