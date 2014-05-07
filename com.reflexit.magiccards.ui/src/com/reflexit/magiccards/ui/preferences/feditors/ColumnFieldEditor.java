package com.reflexit.magiccards.ui.preferences.feditors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;

public class ColumnFieldEditor extends CheckedListEditor {
	private ColumnCollection columns;
	private Button selectButton;
	private Button deselectButton;

	@Override
	protected void parseString(String stringList) {
		columns.updateColumnsFromPropery(stringList);
		int order[] = columns.getColumnsOrder();
		int l = order.length;
		this.list.removeAll();
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

	@Override
	protected void createButtons(Composite box) {
		super.createButtons(box);
		this.selectButton = createPushButton(box, "Select All");//$NON-NLS-1$
		this.deselectButton = createPushButton(box, "Deselect All");//$NON-NLS-1$
		selectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				parseString("+");
			}
		});
		deselectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				parseString("-");
			}
		});
	}

	public ColumnFieldEditor(String name, String labelText, Composite parent, ColumnCollection collection) {
		super(name, labelText, parent, collection.getColumnNames());
		this.columns = collection;
	}

	public ICardField[] getColumnFields() {
		return columns.getColumnFields();
	}

	public String[] getColumnIds() {
		return columns.getColumnIds();
	}
}
