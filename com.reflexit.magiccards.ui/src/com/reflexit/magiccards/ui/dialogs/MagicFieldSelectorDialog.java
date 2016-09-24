package com.reflexit.magiccards.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.reflexit.magiccards.core.exports.CustomExportDelegate;
import com.reflexit.magiccards.ui.preferences.feditors.MagicFieldFieldEditor;

public class MagicFieldSelectorDialog extends Dialog {
	private MagicFieldFieldEditor columnFieldEditor;
	private PreferenceStore store;

	public MagicFieldSelectorDialog(Shell parentShell, PreferenceStore store) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.store = store;
	}

	protected void setPreferenceStore(PreferenceStore store) {
		this.store = store;
	}

	public PreferenceStore getPreferenceStore() {
		return store;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Control area = super.createDialogArea(parent);
		Composite columns = new Composite((Composite) area, SWT.NONE);
		// GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		// layoutData.horizontalSpan = 2;
		// columns.setLayoutData(layoutData);
		columnFieldEditor = new MagicFieldFieldEditor(getPreferenceId(), getLabelText(), columns);
		columnFieldEditor.setPreferenceStore(store);
		columnFieldEditor.load();
		GridData listData = new GridData(GridData.FILL_HORIZONTAL);
		listData.heightHint = 200;
		columnFieldEditor.getListControl(columns).setLayoutData(listData);
		return area;
	}

	protected String getPreferenceId() {
		return CustomExportDelegate.ROW_FIELDS;
	}

	protected String getLabelText() {
		return "Visible Columns and Order";
	}

	@Override
	protected void okPressed() {
		columnFieldEditor.store();
		super.okPressed();
	}
}
