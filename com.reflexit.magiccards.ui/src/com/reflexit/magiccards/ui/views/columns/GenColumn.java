package com.reflexit.magiccards.ui.views.columns;

import com.reflexit.magiccards.core.model.IMagicCard;

public class GenColumn extends ColumnManager {
	private String colName;

	public GenColumn(int i, String columnName) {
		super(i);
		this.colName = columnName;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IMagicCard) {
			IMagicCard card = (IMagicCard) element;
			return card.getByIndex(this.dataIndex);
		}
		return "?";
	}

	@Override
	public String getColumnName() {
		return this.colName;
	}
}
