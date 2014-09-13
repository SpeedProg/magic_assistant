package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.swt.graphics.Image;

import com.reflexit.magiccards.core.model.ICardField;

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

	@Override
	public Image getImage(Object element) {
		return null;
	}

	protected Image getActualImage(Object row) {
		return null;
	}
}
