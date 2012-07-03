package com.reflexit.magiccards.ui.preferences.feditors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;

public class ColumnFieldEditor extends CheckedListEditor {
	private ColumnCollection columns;

	@Override
	protected void parseString(String stringList) {
		columns.updateColumnsFromPropery(stringList);
		int order[] = columns.getColumnsOrder();
		int l = order.length;
		for (int j = 0; j < l; j++) {
			TableItem item = new TableItem(this.list, SWT.NONE);
			AbstractColumn col = columns.getColumn(order[j]);
			item.setData(col.getColumnFullName());
			item.setText(col.getColumnFullName());
			item.setChecked(col.isVisible());
		}
	}

	@Override
	protected String createList() {
		TableItem[] items = list.getItems();
		int order[] = new int[items.length];
		int i = 0;
		for (TableItem tableItem : items) {
			String key = tableItem.getData().toString();
			AbstractColumn col = columns.getColumn(key);
			col.setVisible(tableItem.getChecked());
			order[i++] = col.getColumnIndex();
		}
		columns.setColumnOrder(order);
		return columns.getColumnLayoutProperty();
	}

	public ColumnFieldEditor(String name, String labelText, Composite parent, ColumnCollection collection) {
		super(name, labelText, parent, collection.getColumnNames());
		this.columns = collection;
	}
}
