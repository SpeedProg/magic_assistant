package com.reflexit.magiccards.ui.views.editions;

import com.reflexit.magiccards.core.model.Edition;

final class BlockColumn extends AbstractEditionColumn {
	BlockColumn() {
		super("Block", EditionField.BLOCK);
	}

	@Override
	public String getText(Object element) {
		Edition ed = (Edition) element;
		return ed.getBlock();
	}

	@Override
	public void setText(Edition edition, String string) {
		edition.setBlock(string);
	}
}