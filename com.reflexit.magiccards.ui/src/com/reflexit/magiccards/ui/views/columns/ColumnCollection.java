package com.reflexit.magiccards.ui.views.columns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;

import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.ui.MagicUIActivator;

public abstract class ColumnCollection {
	private List<AbstractColumn> columns;
	protected LinkedHashMap<String, AbstractColumn> order = new LinkedHashMap<String, AbstractColumn>();

	public ColumnCollection() {
		init();
	}

	protected synchronized void init() {
		if (columns == null) {
			List<AbstractColumn> columns = new ArrayList<AbstractColumn>();
			createColumns(columns);
			this.columns = Collections.unmodifiableList(columns);
			order = new LinkedHashMap<String, AbstractColumn>(columns.size());
			setColulmnsIndex();
		}
	}

	public synchronized Collection<AbstractColumn> getColumns() {
		init();
		return this.columns;
	}

	public int getColumnsNumber() {
		init();
		return this.columns.size();
	}

	protected abstract void createColumns(List<AbstractColumn> columns);

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
		updateColumnsFromPropery(propertyValue);
		return getColumnFields();
	}

	public ICardField[] getColumnFields() {
		init();
		ArrayList<ICardField> cf = new ArrayList<ICardField>();
		for (Iterator<AbstractColumn> iterator = order.values().iterator(); iterator.hasNext();) {
			AbstractColumn col = iterator.next();
			if (col.isVisible())
				cf.add(col.getDataField());
		}
		return cf.toArray(new ICardField[cf.size()]);
	}

	public void updateColumnsFromPropery(String value) {
		init();
		String allModifier = "";
		if (value.equals("+") || value.equals("-")) {
			allModifier = value;
			value = "";
		}
		if (value.length() == 0 && allModifier.length() == 0)
			return;
		String[] prefValues = value.split(",");
		LinkedHashMap<String, AbstractColumn> oldorder = order;
		order = new LinkedHashMap<String, AbstractColumn>();
		for (int i = 0; i < prefValues.length; i++) {
			String line = prefValues[i];
			if (line.trim().length() == 0)
				continue;
			String key = line;
			boolean visible = true;
			if (line.startsWith("-")) {
				visible = false;
				key = line.substring(1);
			}
			int colsep = key.indexOf(':');
			int width = 0;
			if (colsep > 0) {
				try {
					width = Integer.parseInt(key.substring(colsep + 1));
					key = key.substring(0, colsep);
				} catch (RuntimeException e) {
					// did not work bad record, ignore it
				}
			}
			AbstractColumn column = oldorder.get(key);
			if (column == null) {
				String colname = key;
				// how did manage to have column which is not in the set?
				try {
					column = columns.stream().filter((c) -> c.getColumnFullName().equals(colname)).findAny().get();
				} catch (NoSuchElementException e) {
					MagicUIActivator.log("Cannot find column " + colname);
					continue;
				}
			}
			if (colsep < 0) {
				width = column.getColumnWidth();
			}
			column.setVisible(visible);
			if (width > 0)
				column.setUserWidth(width);
			order.put(key, column);
			oldorder.remove(key);
		}
		// now deal with leftovers
		boolean visible = allModifier.equals("+");
		for (Iterator<String> iterator = oldorder.keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			AbstractColumn column = oldorder.get(key);
			column.setVisible(visible);
			order.put(key, column);
		}
	}

	public void setColumnOrder(int aorder[]) {
		init();
		LinkedHashMap<String, AbstractColumn> oldorder = order;
		order = new LinkedHashMap<String, AbstractColumn>();
		for (int i = 0; i < aorder.length; i++) {
			int index = aorder[i];
			if (index >= columns.size())
				continue;
			AbstractColumn column = getColumn(index);
			String key = column.getColumnFullName();
			order.put(key, column);
			oldorder.remove(key);
		}
		// now deal with leftovers
		for (Iterator<String> iterator = oldorder.keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			AbstractColumn column = oldorder.get(key);
			column.setVisible(false);
			order.put(key, column);
		}
	}

	/**
	 * Get column by its name
	 * 
	 * @param key
	 * @return
	 */
	public AbstractColumn getColumn(String key) {
		init();
		return order.get(key);
	}

	public AbstractColumn getColumn(ICardField field) {
		init();
		for (Iterator<AbstractColumn> iterator = columns.iterator(); iterator.hasNext();) {
			AbstractColumn column = iterator.next();
			if (column.getDataField() == field)
				return column;
		}
		return null;
	}

	public void moveColumnOnTop(AbstractColumn acolumn) {
		init();
		LinkedHashMap<String, AbstractColumn> oldorder = order;
		order = new LinkedHashMap<String, AbstractColumn>();
		oldorder.remove(acolumn.getColumnFullName());
		order.put(acolumn.getColumnFullName(), acolumn);
		order.putAll(oldorder);
	}

	public String getColumnLayoutProperty() {
		init();
		String line = "";
		for (Iterator<AbstractColumn> iterator = order.values().iterator(); iterator.hasNext();) {
			AbstractColumn column = iterator.next();
			String key = column.getColumnFullName();
			key += ":" + column.getUserWidth();
			if (!column.isVisible()) {
				line += "-" + key;
			} else {
				line += key;
			}
			line += ",";
		}
		if (line.endsWith(","))
			return line.substring(0, line.length() - 1);
		return line;
	}

	public int[] getColumnsOrder() {
		init();
		int ordera[] = new int[order.size()];
		int i = 0;
		for (Iterator<String> iterator = order.keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			AbstractColumn column = order.get(key);
			ordera[i++] = column.getColumnIndex();
		}
		return ordera;
	}

	private void setColulmnsIndex() {
		init();
		int j = 0;
		order.clear();
		for (Iterator<AbstractColumn> iterator = columns.iterator(); iterator.hasNext();) {
			AbstractColumn col = iterator.next();
			col.setColumnIndex(j++);
			order.put(col.getColumnFullName(), col);
			if (order.size() != j)
				throw new IllegalArgumentException("Two diffrent columns have same name");
		}
	}

	public AbstractColumn getColumn(int i) {
		init();
		return columns.get(i);
	}

	public String getId() {
		return null;
	}
}
