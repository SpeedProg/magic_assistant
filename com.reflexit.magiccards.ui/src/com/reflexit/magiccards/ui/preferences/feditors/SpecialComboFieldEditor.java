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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A field editor for a combo box that allows the drop-down selection of one of a list of items.
 * 
 * @since 3.3
 */
public class SpecialComboFieldEditor extends FieldEditor {
	/**
	 * The <code>Combo</code> widget.
	 */
	private Combo fCombo;
	/**
	 * The value (not the name) of the currently selected item in the Combo widget.
	 */
	private String fValue;
	/**
	 * The names (labels) and underlying values to populate the combo widget. These should be
	 * arranged as: { {name1, value1}, {name2, value2}, ...}
	 */
	private String[][] fEntryNamesAndValues;
	private int modifiers;

	/**
	 * Create the combo box field editor.
	 * 
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param labelText
	 *            the label text of the field editor
	 * @param entryNamesAndValues
	 *            the names (labels) and underlying values to populate the combo widget. These
	 *            should be arranged as: { {name1, value1}, {name2, value2}, ...}
	 * @param parent
	 *            the parent composite
	 */
	public SpecialComboFieldEditor(String name, String labelText, String[][] entryNamesAndValues,
			Composite parent, int modifiers) {
		init(name, labelText);
		Assert.isTrue(checkArray(entryNamesAndValues));
		this.fEntryNamesAndValues = entryNamesAndValues;
		this.modifiers = modifiers;
		createControl(parent);
	}

	/**
	 * Checks whether given <code>String[][]</code> is of "type" <code>String[][2]</code>.
	 * 
	 * @return <code>true</code> if it is ok, and <code>false</code> otherwise
	 */
	private boolean checkArray(String[][] table) {
		if (table == null) {
			return false;
		}
		for (int i = 0; i < table.length; i++) {
			String[] array = table[i];
			if (array == null || array.length != 2) {
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
	 */
	@Override
	protected void adjustForNumColumns(int numColumns) {
		if (numColumns > 1) {
			Control control = getLabelControl();
			int left = numColumns;
			if (control != null) {
				((GridData) control.getLayoutData()).horizontalSpan = 1;
				left = left - 1;
			}
			((GridData) this.fCombo.getLayoutData()).horizontalSpan = left;
		} else {
			Control control = getLabelControl();
			if (control != null) {
				((GridData) control.getLayoutData()).horizontalSpan = 1;
			}
			((GridData) this.fCombo.getLayoutData()).horizontalSpan = 1;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.FieldEditor#doFillIntoGrid(org.eclipse.swt.widgets.Composite,
	 * int)
	 */
	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		int comboC = 1;
		if (numColumns > 1) {
			comboC = numColumns - 1;
		}
		Control control = getLabelControl(parent);
		GridData gd = new GridData();
		gd.horizontalSpan = 1;
		control.setLayoutData(gd);
		control = getComboBoxControl(parent);
		gd = new GridData();
		gd.horizontalSpan = comboC;
		gd.horizontalAlignment = GridData.FILL;
		control.setLayoutData(gd);
		control.setFont(parent.getFont());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditor#doLoad()
	 */
	@Override
	protected void doLoad() {
		updateComboForValue(getPreferenceStore().getString(getPreferenceName()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
	 */
	@Override
	protected void doLoadDefault() {
		updateComboForValue(getPreferenceStore().getDefaultString(getPreferenceName()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditor#doStore()
	 */
	@Override
	protected void doStore() {
		if (this.fValue == null) {
			getPreferenceStore().setToDefault(getPreferenceName());
			return;
		}
		getPreferenceStore().setValue(getPreferenceName(), this.fValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditor#getNumberOfControls()
	 */
	@Override
	public int getNumberOfControls() {
		return 2;
	}

	public String getStringValue() {
		return this.fValue;
	}

	/*
	 * Lazily create and return the Combo control.
	 */
	private Combo getComboBoxControl(Composite parent) {
		if (this.fCombo == null) {
			this.fCombo = new Combo(parent, this.modifiers);
			this.fCombo.setFont(parent.getFont());
			for (int i = 0; i < this.fEntryNamesAndValues.length; i++) {
				this.fCombo.add(this.fEntryNamesAndValues[i][0], i);
			}
			this.fCombo.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent evt) {
					String oldValue = SpecialComboFieldEditor.this.fValue;
					String name = SpecialComboFieldEditor.this.fCombo.getText();
					SpecialComboFieldEditor.this.fValue = getValueForName(name);
					setPresentsDefaultValue(false);
					fireValueChanged(VALUE, oldValue, SpecialComboFieldEditor.this.fValue);
				}
			});
			this.fCombo.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					String oldValue = SpecialComboFieldEditor.this.fValue;
					SpecialComboFieldEditor.this.fValue = SpecialComboFieldEditor.this.fCombo.getText();
					setPresentsDefaultValue(false);
					fireValueChanged(VALUE, oldValue, SpecialComboFieldEditor.this.fValue);
				}
			});
		}
		return this.fCombo;
	}

	/*
	 * Given the name (label) of an entry, return the corresponding value.
	 */
	private String getValueForName(String name) {
		for (int i = 0; i < this.fEntryNamesAndValues.length; i++) {
			String[] entry = this.fEntryNamesAndValues[i];
			if (name.equals(entry[0])) {
				return entry[1];
			}
		}
		return name;
	}

	/*
	 * Set the name in the combo widget to match the specified value.
	 */
	private void updateComboForValue(String value) {
		this.fValue = value;
		for (int i = 0; i < this.fEntryNamesAndValues.length; i++) {
			if (value.equals(this.fEntryNamesAndValues[i][1])) {
				this.fCombo.setText(this.fEntryNamesAndValues[i][0]);
				return;
			}
		}
		if (this.fEntryNamesAndValues.length > 0) {
			this.fValue = this.fEntryNamesAndValues[0][1];
			this.fCombo.setText(this.fEntryNamesAndValues[0][0]);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditor#setEnabled(boolean,
	 * org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void setEnabled(boolean enabled, Composite parent) {
		super.setEnabled(enabled, parent);
		getComboBoxControl(parent).setEnabled(enabled);
	}

	public Combo getComboControl() {
		return fCombo;
	}
}
