package com.reflexit.magiccards.ui.preferences.feditors;

import java.util.Arrays;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public abstract class StringListFieldEditor extends ListEditor2 {
	private Button editButton;

	public StringListFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
	}

	@Override
	protected String[] parseString(String stringList) {
		String ids[] = stringList.split(",");
		return ids;
	}

	@Override
	protected void createButtons(Composite box) {
		super.createButtons(box);
		getUpButton().setVisible(false);
		getDownButton().setVisible(false);
		editButton = createPushButton(box, "Edit...");
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editPressed();
			}
		});
	}

	@Override
	protected void removePressed() {
		int index = list.getSelectionIndex();
		if (index >= 0) {
			if (removeElements(list.getSelection())) {
				super.removePressed();
			}
		}
	}

	protected boolean removeElements(String[] selection) {
		// override to react
		return true;
	}

	protected void editPressed() {
		int[] selectionIndices = list.getSelectionIndices();
		String[] selection = list.getSelection();
		editElements(selection);
		for (int i = 0; i < selectionIndices.length; i++) {
			int j = selectionIndices[i];
			list.setItem(j, selection[i]);
		}
		selectionChanged();
	}

	protected abstract void editElements(String[] selection);

	@Override
	protected String createList(String[] items) {
		return Arrays.toString(items).replace(", ", ",").replaceAll("[\\[\\]]", "");
	}

	public Button getEditButton() {
		return editButton;
	}
}
