package com.reflexit.magiccards.ui.views.columns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public abstract class ColumnCollection {
	protected ArrayList<AbstractColumn> columns = new ArrayList<AbstractColumn>();

	public Collection<AbstractColumn> getColumns() {
		return this.columns;
	}

	public int getColumnsNumber() {
		return this.columns.size();
	}

	public void createColumnLabelProviders() {
		createColumns();
		setColulmnsIndex();
	}

	protected abstract void createColumns();

	public String[] getColumnNames() {
		int i = 0;
		String[] columnNames = new String[getColumnsNumber()];
		for (Iterator<AbstractColumn> iterator = getColumns().iterator(); iterator.hasNext();) {
			AbstractColumn col = iterator.next();
			columnNames[i++] = col.getColumnFullName();
		}
		return columnNames;
	}

	public String[] getColumnIds() {
		int i = 0;
		String[] columnNames = new String[getColumnsNumber()];
		for (Iterator<AbstractColumn> iterator = getColumns().iterator(); iterator.hasNext();) {
			AbstractColumn col = iterator.next();
			columnNames[i++] = col.getDataField().toString();
		}
		return columnNames;
	}

	protected void setColulmnsIndex() {
		int j = 0;
		for (Iterator<AbstractColumn> iterator = columns.iterator(); iterator.hasNext();) {
			AbstractColumn col = iterator.next();
			col.setColumnIndex(j++);
		}
	}

	public AbstractColumn getColumn(int i) {
		return columns.get(i);
	}
}
