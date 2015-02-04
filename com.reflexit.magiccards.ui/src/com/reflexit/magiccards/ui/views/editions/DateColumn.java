package com.reflexit.magiccards.ui.views.editions;

import java.text.ParseException;

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Editions.Edition;

final class DateColumn extends AbstractEditionColumn {
	public DateColumn() {
		super("Date", EditionField.DATE);
	}

	@Override
	public String getText(Object element) {
		Editions.Edition ed = (Edition) element;
		if (ed.getReleaseDate() == null)
			return "?";
		return EditionsComposite.formatter.format(ed.getReleaseDate());
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