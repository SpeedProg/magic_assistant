package com.reflexit.magiccards.ui.preferences.feditors;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardField;

public class MagicFieldFieldEditor extends CheckedListEditor {
	private Map<String, ICardField> allFields = new LinkedHashMap<String, ICardField>();
	{
		ICardField[] allFieldsArr = MagicCardField.allFields();
		for (int i = 0; i < allFieldsArr.length; i++) {
			ICardField f = allFieldsArr[i];
			allFields.put(f.name(), f);
		}
	}
	private Button selectButton;
	private Button deselectButton;

	@Override
	protected void parseString(String stringList) {
		Map<String, ICardField> curFields = new LinkedHashMap<String, ICardField>();
		this.list.removeAll();
		if (stringList.length() > 0 && !stringList.equals("+") && !stringList.equals("-")) {
			String ids[] = stringList.split(",");
			for (int j = 0; j < ids.length; j++) {
				String name = ids[j];
				ICardField field = MagicCardField.fieldByName(name);
				createTreeItem(field, true);
				curFields.put(field.name(), field);
			}
		}
		boolean enabled = stringList.equals("+");
		for (String name : allFields.keySet()) {
			if (curFields.containsKey(name))
				continue;
			ICardField field = allFields.get(name);
			createTreeItem(field, enabled);
		}
	}

	public TableItem createTreeItem(ICardField field, boolean checked) {
		TableItem item = new TableItem(this.list, SWT.NONE);
		item.setData(field);
		item.setText(field.getLabel());
		item.setChecked(checked);
		return item;
	}

	@Override
	protected String createList() {
		TableItem[] items = list.getItems();
		String res = "";
		for (TableItem tableItem : items) {
			ICardField field = (ICardField) tableItem.getData();
			if (tableItem.getChecked()) {
				res += field.name() + ",";
			}
		}
		if (res.length() > 0)
			return res.substring(0, res.length() - 1);
		return "";
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

	public MagicFieldFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent, new String[] {});
	}
}
