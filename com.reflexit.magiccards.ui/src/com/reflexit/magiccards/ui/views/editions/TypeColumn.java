package com.reflexit.magiccards.ui.views.editions;

import com.reflexit.magiccards.core.model.Edition;

final class TypeColumn extends AbstractEditionColumn {
	TypeColumn() {
		super("Type", EditionField.TYPE);
	}

	@Override
	public String getText(Object element) {
		Edition ed = (Edition) element;
		return ed.getType();
	}

	@Override
	public void setText(Edition edition, String string) {
		edition.setType(string);
	}
}