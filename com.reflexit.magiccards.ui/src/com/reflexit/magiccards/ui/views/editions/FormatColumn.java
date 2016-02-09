package com.reflexit.magiccards.ui.views.editions;

import com.reflexit.magiccards.core.legality.Format;
import com.reflexit.magiccards.core.model.Edition;

final class FormatColumn extends AbstractEditionColumn {
	FormatColumn() {
		super("Format", EditionField.FORMAT);
	}

	@Override
	public String getText(Object element) {
		Edition ed = (Edition) element;
		Format firstLegal = ed.getLegalityMap().getFirstLegal();
		if (firstLegal == null)
			return Format.LEGACY.name();
		return firstLegal.name();
	}

	@Override
	public String getTextForEdit(Edition element) {
		return element.getFormatString();
	}

	@Override
	public void setText(Edition edition, String string) {
		edition.setFormats(string);
	}
}