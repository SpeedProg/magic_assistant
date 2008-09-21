package com.reflexit.magiccards.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.widgets.EditionsComposite;

public class EditionsFilterPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private EditionsComposite comp;

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
	protected Control createContents(Composite parent) {
		this.comp = new EditionsComposite(parent, SWT.CHECK | SWT.BORDER);
		this.comp.setPreferenceStore(getPreferenceStore());
		this.comp.initialize();
		return this.comp;
	}

	@Override
	public boolean performOk() {
		if (this.comp != null)
			this.comp.performApply();
		return true;
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return MagicUIActivator.getDefault().getPreferenceStore();
	}
}
