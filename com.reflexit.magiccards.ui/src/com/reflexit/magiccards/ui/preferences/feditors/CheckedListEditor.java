/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.ui.preferences.feditors;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

/**
 * An abstract field editor that manages a list of input values. The editor displays a list
 * containing the values, and Up and Down buttons to adjust the order of elements in the list.
 * <p>
 * Subclasses must implement the <code>parseString</code>, <code>createList</code>, and
 * <code>getNewInputObject</code> framework methods.
 * </p>
 */
public class CheckedListEditor extends FieldEditor {
	/**
	 * The list widget; <code>null</code> if none (before creation or after disposal).
	 */
	protected Table list;
	/**
	 * The button box containing the Add, Remove, Up, and Down buttons; <code>null</code> if none
	 * (before creation or after disposal).
	 */
	private Composite buttonBox;
	/**
	 * The Up button.
	 */
	private Button upButton;
	/**
	 * The Down button.
	 */
	private Button downButton;
	/**
	 * The selection listener.
	 */
	private SelectionListener selectionListener;
	private LinkedHashMap<String, String> keysValus = new LinkedHashMap<String, String>();

	/**
	 * Creates a new list field editor
	 */
	protected CheckedListEditor() {
	}

	/**
	 * Creates a list field editor.
	 * 
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param labelText
	 *            the label text of the field editor
	 * @param parent
	 *            the parent of the field editor's control
	 */
	public CheckedListEditor(String name, String labelText, Composite parent, String[] values) {
		this(name, labelText, parent, values, values);
	}

	public CheckedListEditor(String name, String labelText, Composite parent, String[] values, String[] keys) {
		init(name, labelText);
		createControl(parent);
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			keysValus.put(key, values[i]);
		}
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	@Override
	protected void adjustForNumColumns(int numColumns) {
		Control control = getLabelControl();
		((GridData) control.getLayoutData()).horizontalSpan = numColumns;
		((GridData) this.list.getLayoutData()).horizontalSpan = numColumns - 1;
	}

	/**
	 * Creates the Add, Remove, Up, and Down button in the given button box.
	 * 
	 * @param box
	 *            the box for the buttons
	 */
	protected void createButtons(Composite box) {
		this.upButton = createPushButton(box, "Up");//$NON-NLS-1$
		this.downButton = createPushButton(box, "Down");//$NON-NLS-1$
	}

	/**
	 * Helper method to create a push button.
	 * 
	 * @param parent
	 *            the parent control
	 * @param key
	 *            the resource name used to supply the button's label text
	 * @return Button
	 */
	protected Button createPushButton(Composite parent, String key) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(JFaceResources.getString(key));
		button.setFont(parent.getFont());
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		int widthHint = convertHorizontalDLUsToPixels(button, IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		button.setLayoutData(data);
		button.addSelectionListener(getSelectionListener());
		return button;
	}

	/**
	 * Creates a selection listener.
	 */
	public void createSelectionListener() {
		this.selectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				Widget widget = event.widget;
				if (widget == CheckedListEditor.this.upButton) {
					upPressed();
				} else if (widget == CheckedListEditor.this.downButton) {
					downPressed();
				} else if (widget == CheckedListEditor.this.list) {
					selectionChanged();
				}
			}
		};
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		Control control = getLabelControl(parent);
		GridData gd = new GridData();
		gd.horizontalSpan = numColumns;
		control.setLayoutData(gd);
		this.list = getListControl(parent);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = GridData.BEGINNING;
		gd.heightHint = 400;
		gd.horizontalSpan = numColumns - 1;
		gd.grabExcessHorizontalSpace = true;
		this.list.setLayoutData(gd);
		this.buttonBox = getButtonBoxControl(parent);
		gd = new GridData();
		gd.verticalAlignment = GridData.BEGINNING;
		this.buttonBox.setLayoutData(gd);
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	@Override
	protected void doLoad() {
		if (this.list != null) {
			String s = getPreferenceStore().getString(getPreferenceName());
			parseString(s);
		}
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	@Override
	protected void doLoadDefault() {
		if (this.list != null) {
			this.list.removeAll();
			String s = getPreferenceStore().getDefaultString(getPreferenceName());
			parseString(s);
		}
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	@Override
	protected void doStore() {
		String s = createList();
		if (s != null) {
			getPreferenceStore().setValue(getPreferenceName(), s);
		}
	}

	/**
	 * Notifies that the Down button has been pressed.
	 */
	private void downPressed() {
		swap(false);
	}

	/**
	 * Returns this field editor's button box containing the Add, Remove, Up, and Down button.
	 * 
	 * @param parent
	 *            the parent control
	 * @return the button box
	 */
	public Composite getButtonBoxControl(Composite parent) {
		if (this.buttonBox == null) {
			this.buttonBox = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout();
			layout.marginWidth = 0;
			this.buttonBox.setLayout(layout);
			createButtons(this.buttonBox);
			this.buttonBox.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent event) {
					CheckedListEditor.this.upButton = null;
					CheckedListEditor.this.downButton = null;
					CheckedListEditor.this.buttonBox = null;
				}
			});
		} else {
			checkParent(this.buttonBox, parent);
		}
		selectionChanged();
		return this.buttonBox;
	}

	/**
	 * Returns this field editor's list control.
	 * 
	 * @param parent
	 *            the parent control
	 * @return the list control
	 */
	public Table getListControl(Composite parent) {
		if (this.list == null) {
			this.list = new Table(parent, SWT.BORDER | SWT.CHECK | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
			this.list.setFont(parent.getFont());
			this.list.addSelectionListener(getSelectionListener());
			this.list.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent event) {
					CheckedListEditor.this.list = null;
				}
			});
		} else {
			checkParent(this.list, parent);
		}
		return this.list;
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	@Override
	public int getNumberOfControls() {
		return 2;
	}

	/**
	 * Returns this field editor's selection listener. The listener is created if nessessary.
	 * 
	 * @return the selection listener
	 */
	private SelectionListener getSelectionListener() {
		if (this.selectionListener == null) {
			createSelectionListener();
		}
		return this.selectionListener;
	}

	/**
	 * Returns this field editor's shell.
	 * <p>
	 * This method is internal to the framework; subclassers should not call this method.
	 * </p>
	 * 
	 * @return the shell
	 */
	protected Shell getShell() {
		if (this.upButton == null) {
			return null;
		}
		return this.upButton.getShell();
	}

	/**
	 * Splits the given string into a list of strings. This method is the converse of <code>createList</code>.
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 * 
	 * @param stringList
	 *            the string
	 * @return an array of <code>String</code>
	 * @see #createList
	 */
	protected void parseString(String stringList) {
		String[] prefValues = stringList.split(",");
		LinkedHashSet<String> prefs = new LinkedHashSet<String>();
		prefs.addAll(Arrays.asList(prefValues));
		@SuppressWarnings("unchecked")
		LinkedHashMap<String, String> rem = (LinkedHashMap<String, String>) keysValus.clone();
		for (String k : prefValues) {
			if (k.startsWith("-")) {
				k = k.substring(1);
			}
			rem.remove(k);
		}
		// add field which are in the list for the control but not in the
		// preferences
		for (String k : rem.keySet()) {
			prefs.add("-" + k);
		}
		for (String k : prefs) {
			boolean checked = true;
			if (k.startsWith("-")) {
				checked = false;
				k = k.substring(1);
			}
			TableItem item = new TableItem(this.list, SWT.NONE);
			item.setData(k);
			String value = keysValus.get(k);
			if (value == null) {
				value = k;
			}
			item.setText(value);
			item.setChecked(checked);
		}
	}

	/**
	 * Combines the given list of items into a single string. This method is the converse of
	 * <code>parseString</code>.
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 * 
	 * @return the combined string
	 * @see #parseString
	 */
	protected String createList() {
		String res = "";
		TableItem[] items = this.list.getItems();
		for (TableItem tableItem : items) {
			String text;
			if (tableItem.getData() != null) {
				text = tableItem.getData().toString();
			} else {
				text = tableItem.getText();
			}
			res += tableItem.getChecked() ? "" : "-";
			res += text;
			res += ",";
		}
		return res;
	}

	/**
	 * Notifies that the list selection has changed.
	 */
	private void selectionChanged() {
		int index = this.list.getSelectionIndex();
		int size = this.list.getItemCount();
		this.upButton.setEnabled(size > 1 && index > 0);
		this.downButton.setEnabled(size > 1 && index >= 0 && index < size - 1);
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	@Override
	public void setFocus() {
		if (this.list != null) {
			this.list.setFocus();
		}
	}

	/**
	 * Moves the currently selected item up or down.
	 * 
	 * @param up
	 *            <code>true</code> if the item should move up, and <code>false</code> if it should
	 *            move down
	 */
	private void swap(boolean up) {
		setPresentsDefaultValue(false);
		int index = this.list.getSelectionIndex();
		int target = up ? index - 1 : index + 1;
		if (index >= 0) {
			TableItem item = this.list.getItem(index);
			String text = item.getText();
			boolean check = item.getChecked();
			Object data = item.getData();
			TableItem targetItem = this.list.getItem(target);
			item.setText(targetItem.getText());
			item.setChecked(targetItem.getChecked());
			item.setData(targetItem.getData());
			targetItem.setText(text);
			targetItem.setChecked(check);
			targetItem.setData(data);
			this.list.setSelection(target);
		}
		selectionChanged();
	}

	/**
	 * Notifies that the Up button has been pressed.
	 */
	private void upPressed() {
		swap(true);
	}

	/*
	 * @see FieldEditor.setEnabled(boolean,Composite).
	 */
	@Override
	public void setEnabled(boolean enabled, Composite parent) {
		super.setEnabled(enabled, parent);
		getListControl(parent).setEnabled(enabled);
		this.upButton.setEnabled(enabled);
		this.downButton.setEnabled(enabled);
	}
}
