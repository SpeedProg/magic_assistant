package com.reflexit.magiccards.ui.views.columns;

import com.reflexit.magiccards.core.model.IMagicCard;

public class IdColumn extends ColumnManager {
	public IdColumn(int column) {
		super(column);
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IMagicCard) {
			IMagicCard card = (IMagicCard) element;
			return String.valueOf(card.getCardId());
		}
		return "?";
	}

	@Override
	public int getColumnWidth() {
		return 60;
	}

	@Override
	public String getColumnName() {
		return "Card Id";
	}
}
