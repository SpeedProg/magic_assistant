package com.reflexit.magiccards.ui.views.editions;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;

public class AbstractEditionColumn extends ColumnLabelProvider {
	protected int columnIndex = -1;
	private String name;
	private EditionField field;

	public AbstractEditionColumn(String name, EditionField field) {
		this.name = name;
		this.field = field;
	}

	public int getColumnIndex() {
		return columnIndex;
	}

	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}

	public String getColumnName() {
		return name;
	}

	public String getColumnTooltip() {
		if (getColumnName().equals(getColumnFullName()))
			return "";
		else
			return getColumnFullName();
	}

	public int getColumnWidth() {
		return 100;
	}

	public String getColumnFullName() {
		return getColumnName();
	}

	/**
	 * @param viewer
	 * @return
	 */
	public EditingSupport getEditingSupport(ColumnViewer viewer) {
		return null;
	}

	public EditionField getSortField() {
		return field;
	}
}
