package com.reflexit.magiccards.ui.views.columns;

import com.reflexit.magiccards.core.model.ICardField;

public class PowerColumn extends GenColumn {
	private String fullname;

	public PowerColumn(ICardField field, String columnName, String fullname) {
		super(field, columnName);
		this.fullname = fullname;
	}

	@Override
	public String getColumnFullName() {
		return this.fullname;
	}

	@Override
	public int getColumnWidth() {
		return 30;
	}
}
