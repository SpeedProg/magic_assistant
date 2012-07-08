package com.reflexit.magiccards.ui.views.columns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TreeColumn;

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
			createColumnLabelProviders();
		updateColumnsFromPropery(propertyValue);
		ArrayList<ICardField> cf = new ArrayList<ICardField>();
		for (Iterator iterator = order.values().iterator(); iterator.hasNext();) {
			AbstractColumn col = (AbstractColumn) iterator.next();
			if (col.isVisible())
				cf.add(col.getDataField());
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
				// how did manage to have column which is not in the set?
				// lets pretend it never happened
				continue;
			}
			column.setVisible(visible);
			if (width > 0)
				column.setUserWidth(width);
			order.put(key, column);
			oldorder.remove(key);
		}
		// now deal with leftovers
		for (Iterator<String> iterator = oldorder.keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			AbstractColumn column = oldorder.get(key);
			column.setVisible(!true);
			order.put(key, column);
		}
	}

	public void setColumnOrder(int aorder[]) {
		LinkedHashMap<String, AbstractColumn> oldorder = order;
		order = new LinkedHashMap<String, AbstractColumn>();
		for (int i = 0; i < aorder.length; i++) {
			int index = aorder[i];
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

	public AbstractColumn getColumn(String key) {
		return order.get(key);
	}

	public void moveColumnOnTop(AbstractColumn acolumn) {
		LinkedHashMap<String, AbstractColumn> oldorder = order;
		order = new LinkedHashMap<String, AbstractColumn>();
		oldorder.remove(acolumn.getColumnFullName());
		order.put(acolumn.getColumnFullName(), acolumn);
		order.putAll(oldorder);
	}

	public String getColumnLayoutProperty() {
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

	public void setColumnProperties(TreeColumn[] acolumns) {
		for (int i = 0; i < acolumns.length; i++) {
			TreeColumn acol = acolumns[i];
			AbstractColumn mcol = getColumn(i);
			int w = acol.getWidth();
			if (w > 0) {
				mcol.setUserWidth(w);
				mcol.setVisible(true);
			} else
				mcol.setVisible(false);
		}
	}

	public void setColumnProperties(TableColumn[] acolumns) {
		for (int i = 0; i < acolumns.length; i++) {
			TableColumn acol = acolumns[i];
			AbstractColumn mcol = getColumn(i);
			int w = acol.getWidth();
			if (w > 0) {
				mcol.setUserWidth(w);
				mcol.setVisible(true);
			} else
				mcol.setVisible(false);
		}
	}
}
