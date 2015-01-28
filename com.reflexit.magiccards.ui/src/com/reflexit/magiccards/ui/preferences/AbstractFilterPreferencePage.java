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

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.reflexit.magiccards.ui.dialogs.CardFilterDialog;
import com.reflexit.magiccards.ui.preferences.feditors.MFieldEditorPreferencePage;

/**
 * Generic filter preference page
 */
public abstract class AbstractFilterPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	protected Collection<MFieldEditorPreferencePage> subPages = new ArrayList();
	protected CardFilterDialog dialog;

	public AbstractFilterPreferencePage(CardFilterDialog dialog) {
		this.dialog = dialog;
	}

	@Override
	public void init(IWorkbench workbench) {
		// nothing
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		getDefaultsButton().setText("Restore Page Defaults");
		((GridData) (getDefaultsButton().getLayoutData())).widthHint = SWT.DEFAULT;
	}

	@Override
	protected void contributeButtons(Composite buttonBar) {
		if (dialog != null) {
			GridLayout layout = (GridLayout) buttonBar.getLayout();
			layout.numColumns = layout.numColumns + 1;
			int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
			Button defaultsGlobalButton = new Button(buttonBar, SWT.PUSH);
			defaultsGlobalButton.setText("Restore All Defaults");
			Dialog.applyDialogFont(defaultsGlobalButton);
			GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			Point minButtonSize = defaultsGlobalButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
			data.widthHint = Math.max(widthHint, minButtonSize.x);
			defaultsGlobalButton.setLayoutData(data);
			defaultsGlobalButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					performGlobalDefaults();
				}
			});
		}
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
	public void performDefaults() {
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
		Control control = subPage.getControl();
		control.setLayoutData(gd);
		// setFontRec(control, parent.getFont());
		this.subPages.add(subPage);
	}

	public void setFontRec(Control control, Font font) {
		control.setFont(getFont());
		if (control instanceof Composite) {
			Control[] children = ((Composite) control).getChildren();
			for (int i = 0; i < children.length; i++) {
				Control s = children[i];
				s.setFont(getFont());
				setFontRec(s, font);
			}
		}
	}

	@Override
	public boolean performOk() {
		for (Object element : this.subPages) {
			PreferencePage page = (PreferencePage) element;
			page.performOk();
		}
		return super.performOk();
	}

	public void performGlobalDefaults() {
		if (dialog != null) {
			dialog.performDefaults();
			dialog.refresh();
		}
	}
}
