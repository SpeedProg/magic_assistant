package com.reflexit.magiccards.ui.views.columns;

import org.eclipse.jface.viewers.ColumnLabelProvider;

public abstract class ColumnManager extends ColumnLabelProvider {
	protected int dataIndex;

	public ColumnManager(int dataIndex) {
		this.dataIndex = dataIndex;
	}

	public abstract String getColumnName();

	public String getColumnTooltip() {
		if (getColumnName().equals(getColumnFullName()))
			return "";
		else
			return getColumnFullName();
	}

	public int getColumnWidth() {
		return 100;
	}

	public int getDataIndex() {
		return this.dataIndex;
	}

	public String getColumnFullName() {
		return getColumnName();
	};
}
