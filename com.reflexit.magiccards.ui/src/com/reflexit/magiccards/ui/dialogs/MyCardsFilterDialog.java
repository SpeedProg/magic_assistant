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
package com.reflexit.magiccards.ui.dialogs;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.reflexit.magiccards.ui.preferences.LocationFilterPreferencePage;
import com.reflexit.magiccards.ui.preferences.UserFilterPreferencePage;

/**
 * Filter dialog for My Cards view
 */
public class MyCardsFilterDialog extends CardFilterDialog {
	public MyCardsFilterDialog(Shell parentShell, IPreferenceStore store) {
		super(parentShell, store);
		addNode(new PreferenceNode("locations", new LocationFilterPreferencePage(SWT.MULTI)));
		addNode(new PreferenceNode("user", new UserFilterPreferencePage(this)));
		super.addSavePage();
	}

	@Override
	protected void addSavePage() {
		// overload not add here
	}
}
