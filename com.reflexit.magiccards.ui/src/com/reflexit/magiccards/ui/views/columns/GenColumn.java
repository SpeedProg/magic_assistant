package com.reflexit.magiccards.ui.views.columns;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;

public class GenColumn extends ColumnManager {
	private String colName;

	public GenColumn(int i, String columnName) {
		super(i);
		this.colName = columnName;
	}

	public String getText(Object element) {
		if (element instanceof IMagicCard) {
			IMagicCard card = (IMagicCard) element;
			return card.getByIndex(this.dataIndex);
		}
		if (element instanceof MagicCardPhisical) {
			MagicCardPhisical card = (MagicCardPhisical) element;
			if (this.dataIndex < 11) {
				return getText(card.getCard());
			} else {
				return card.getByIndex(this.dataIndex - 11);
			}
		}
		return "?";
	}

	public String getColumnName() {
		return this.colName;
	}
}
