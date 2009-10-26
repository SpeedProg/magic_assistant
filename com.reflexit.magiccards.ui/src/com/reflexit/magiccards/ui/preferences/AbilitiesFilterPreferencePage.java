package com.reflexit.magiccards.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import java.util.ArrayList;
import java.util.Collection;

import com.reflexit.magiccards.ui.preferences.feditors.AdvancedTextSeachFieldsPreferenceGroup;
import com.reflexit.magiccards.ui.preferences.feditors.MFieldEditorPreferencePage;

public class AbilitiesFilterPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private Composite panel;
	private Collection<MFieldEditorPreferencePage> subPages;

	public AbilitiesFilterPreferencePage() {
		this.subPages = new ArrayList();
		setTitle("Abilities Filter");
		setDescription("This filter allows to perform adavnced abilities search using oracle text. To search for two abilities (using and) enter them on the same line, for alternative abilities (or) use different lines, use 'Excluding' line to remove some cards from search result.");
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
		setTitle("Abilities Filter");
		this.panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		this.panel.setLayout(layout);
		this.panel.setFont(parent.getFont());
		createAndAdd(new AdvancedTextSeachFieldsPreferenceGroup(), panel);
		return this.panel;
	}

	@Override
	protected void performDefaults() {
		for (Object element : this.subPages) {
			MFieldEditorPreferencePage page = (MFieldEditorPreferencePage) element;
			page.performDefaults();
		}
		super.performDefaults();
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		throw new UnsupportedOperationException("Unspecified preference store");
	}

	private void createAndAdd(MFieldEditorPreferencePage subPage, Composite parent) {
		subPage.setPreferenceStore(getPreferenceStore());
		subPage.createControl(parent);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = SWT.BEGINNING;
		subPage.getControl().setLayoutData(gd);
		this.subPages.add(subPage);
	}

	@Override
	public boolean performOk() {
		for (Object element : this.subPages) {
			PreferencePage page = (PreferencePage) element;
			page.performOk();
		}
		return super.performOk();
	}
}
