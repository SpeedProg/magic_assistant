package com.reflexit.magiccards.ui.views.columns;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;

public class IdColumn extends ColumnManager {
	public IdColumn(int column) {
		super(column);
	}

	public String getText(Object element) {
		if (element instanceof IMagicCard) {
			IMagicCard card = (IMagicCard) element;
			return String.valueOf(card.getCardId());
		}
		if (element instanceof MagicCardPhisical) {
			MagicCardPhisical card = (MagicCardPhisical) element;
			return String.valueOf(card.getCard().getCardId());
		}
		return "?";
	}

	public int getColumnWidth() {
		return 60;
	}

	public String getColumnName() {
		return "Card Id";
	}
}
