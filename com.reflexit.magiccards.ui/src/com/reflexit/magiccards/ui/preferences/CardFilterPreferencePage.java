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
import java.util.Iterator;

import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.feditors.ColorsPreferenceGroup;
import com.reflexit.magiccards.ui.preferences.feditors.NumbericalPreferenceGroup;
import com.reflexit.magiccards.ui.preferences.feditors.TextSearchPreferenceGroup;
import com.reflexit.magiccards.ui.preferences.feditors.TypesPreferenceGroup;

public class CardFilterPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private Composite panel;
	private Collection subPages;

	public CardFilterPreferencePage() {
		this.subPages = new ArrayList();
		setTitle("Basic Filter");
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
		setTitle("Card Filter");
		this.panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		this.panel.setLayout(layout);
		this.panel.setFont(parent.getFont());
		Composite first = createColumnComposite(this.panel, 3);
		Composite second = createColumnComposite(this.panel, 1);
		createAndAdd(new TypesPreferenceGroup(), first);
		createAndAdd(new ColorsPreferenceGroup(), first);
		createAndAdd(new TextSearchPreferenceGroup(), second);
		createAndAdd(new NumbericalPreferenceGroup(), first);
		return this.panel;
	}

	private Composite createColumnComposite(Composite parent, int cols) {
		Composite sec = new Composite(parent, SWT.NONE);
		GridLayout layout2row = new GridLayout(cols, false);
		layout2row.marginHeight = 0;
		layout2row.marginWidth = 0;
		sec.setLayout(layout2row);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = SWT.BEGINNING;
		sec.setLayoutData(gd);
		return sec;
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return MagicUIActivator.getDefault().getPreferenceStore();
	}

	private void createAndAdd(PreferencePage subPage, Composite parent) {
		subPage.setPreferenceStore(getPreferenceStore());
		subPage.createControl(parent);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = SWT.BEGINNING;
		subPage.getControl().setLayoutData(gd);
		this.subPages.add(subPage);
	}

	@Override
	public boolean performOk() {
		for (Iterator iterator = this.subPages.iterator(); iterator.hasNext();) {
			PreferencePage page = (PreferencePage) iterator.next();
			page.performOk();
		}
		return super.performOk();
	}
}
