package com.reflexit.magiccards.ui.views.editions;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.reflexit.magiccards.core.model.Edition;

final class DateColumn extends AbstractEditionColumn {
	private final SimpleDateFormat formatter = new SimpleDateFormat("MMMM yyyy");
	public DateColumn() {
		super("Date", EditionField.DATE);
	}

	@Override
	public String getText(Object element) {
		Edition ed = (Edition) element;
		if (ed.getReleaseDate() == null)
			return "?";
		return formatter.format(ed.getReleaseDate());
	}

	@Override
	public void setText(Edition edition, String string) {
		try {
			edition.setReleaseDate(string);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Enter day in format like: December 2010");
		}
	}
}