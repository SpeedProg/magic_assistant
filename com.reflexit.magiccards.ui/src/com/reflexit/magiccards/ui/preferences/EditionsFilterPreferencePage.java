package com.reflexit.magiccards.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.reflexit.magiccards.ui.widgets.EditionsComposite;

public class EditionsFilterPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	public static final String LAST_SET = "onlyLastSet";
	private EditionsComposite comp;
	private Button onlyLastSet;

	public EditionsFilterPreferencePage() {
		setTitle("Set Filter");
		// setDescription("A demonstration of a preference page
		// implementation");
	}

	public void init(IWorkbench workbench) {
		// nothing
	}

	@Override
	public void noDefaultAndApplyButton() {
		super.noDefaultAndApplyButton();
	}

	@Override
	public void setPreferenceStore(IPreferenceStore store) {
		super.setPreferenceStore(store);
		if (comp != null)
			comp.initialize();
	}

	@Override
	protected Control createContents(Composite parent) {
		this.onlyLastSet = new Button(parent, SWT.CHECK);
		this.onlyLastSet.setText("Only show the latest set");
		this.onlyLastSet.setSelection(getPreferenceStore().getBoolean(LAST_SET));
		this.comp = new EditionsComposite(parent, SWT.CHECK | SWT.BORDER, true);
		this.comp.setPreferenceStore(getPreferenceStore());
		this.comp.initialize();
		return this.comp;
	}

	@Override
	public boolean performOk() {
		if (this.comp != null) {
			this.comp.performApply();
			getPreferenceStore().setValue(LAST_SET, onlyLastSet.getSelection());
		}
		return true;
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		throw new UnsupportedOperationException("Unspecified preference store");
	}

	@Override
	public void performDefaults() {
		comp.setToDefaults();
		super.performDefaults();
	}
}
