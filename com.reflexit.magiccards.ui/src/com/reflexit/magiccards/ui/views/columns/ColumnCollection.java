package com.reflexit.magiccards.ui.views.columns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.reflexit.magiccards.core.model.ICardField;

public abstract class ColumnCollection {
	protected ArrayList<AbstractColumn> columns = new ArrayList<AbstractColumn>();
	protected LinkedHashMap<String, AbstractColumn> order = new LinkedHashMap<String, AbstractColumn>();

	public Collection<AbstractColumn> getColumns() {
		return this.columns;
	}

	public int getColumnsNumber() {
		return this.columns.size();
	}

	public void createColumnLabelProviders() {
		createColumns();
		order = new LinkedHashMap<String, AbstractColumn>(columns.size());
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

	public ICardField[] getSelectedColumnFields(String propertyValue) {
		if (columns.size() == 0)
			createColumns();
		ArrayList<ICardField> cf = new ArrayList<ICardField>();
		String names[] = propertyValue.split(",");
		for (int i = 0; i < names.length; i++) {
			String colName = names[i];
			if (colName.startsWith("-"))
				continue;
			for (Iterator<AbstractColumn> iterator = columns.iterator(); iterator.hasNext();) {
				AbstractColumn col = iterator.next();
				if (colName.equals(col.getColumnFullName())) {
					cf.add(col.getDataField());
					break;
				}
			}
		}
		return cf.toArray(new ICardField[cf.size()]);
	}

	public void updateColumnsFromPropery(String value) {
		String[] prefValues = value.split(",");
		if (prefValues.length == 0)
			return;
		LinkedHashMap<String, AbstractColumn> oldorder = order;
		order = new LinkedHashMap<String, AbstractColumn>();
		for (int i = 0; i < prefValues.length; i++) {
			String line = prefValues[i];
			String key = line;
			boolean hidden = false;
			if (line.startsWith("-")) {
				hidden = true;
				key = line.substring(1);
			}
			AbstractColumn column = oldorder.get(key);
			if (column == null) {
				// how did manage to have column which is not in the set?
				// lets pretend it never happened
				continue;
			}
			column.setHidden(hidden);
			order.put(key, column);
			oldorder.remove(key);
		}
		// now deal with leftovers
		for (Iterator<String> iterator = oldorder.keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			AbstractColumn column = oldorder.get(key);
			column.setHidden(true);
			order.put(key, column);
		}
	}

	public int[] getColumnsOrder() {
		int ordera[] = new int[order.size()];
		int i = 0;
		for (Iterator<String> iterator = order.keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			AbstractColumn column = order.get(key);
			ordera[i++] = column.getColumnIndex();
		}
		return ordera;
	}

	protected void setColulmnsIndex() {
		int j = 0;
		for (Iterator<AbstractColumn> iterator = columns.iterator(); iterator.hasNext();) {
			AbstractColumn col = iterator.next();
			col.setColumnIndex(j++);
			order.put(col.getColumnFullName(), col);
		}
	}

	public AbstractColumn getColumn(int i) {
		return columns.get(i);
	}
}
