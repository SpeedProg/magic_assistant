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
import org.eclipse.swt.widgets.Shell;

import com.reflexit.magiccards.ui.preferences.UserFilterPreferencePage;

/**
 * Filter dialog for Decks and Collections
 */
public class DeckFilterDialog extends CardFilterDialog {
	public DeckFilterDialog(Shell parentShell, IPreferenceStore store) {
		super(parentShell, store);
		addNode(new PreferenceNode("user", new UserFilterPreferencePage(this)));
		super.addSavePage();
	}

	@Override
	protected void addSavePage() {
		// overload not add here
	}
}
