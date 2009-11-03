/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import java.util.ArrayList;
import java.util.Collection;

import com.reflexit.magiccards.ui.preferences.feditors.MFieldEditorPreferencePage;

/**
 * Generic filter preference page
 */
public abstract class AbstractFilterPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	protected Composite panel;
	protected Collection<MFieldEditorPreferencePage> subPages = new ArrayList();

	public void init(IWorkbench workbench) {
		// nothing
	}

	@Override
	public void setPreferenceStore(IPreferenceStore store) {
		super.setPreferenceStore(store);
		for (Object element : this.subPages) {
			MFieldEditorPreferencePage page = (MFieldEditorPreferencePage) element;
			page.initialize();
		}
	}

	@Override
	public void noDefaultAndApplyButton() {
		super.noDefaultAndApplyButton();
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

	protected void createAndAdd(MFieldEditorPreferencePage subPage, Composite parent) {
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
