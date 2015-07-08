package com.reflexit.magiccards.ui.views.columns;

import com.reflexit.magiccards.core.model.abs.ICardField;

public class GenColumn extends AbstractColumn {
	private String colName;

	public GenColumn(ICardField field, String columnName) {
		super(field);
		this.colName = columnName;
	}

	@Override
	public String getColumnName() {
		return this.colName;
	}
}
