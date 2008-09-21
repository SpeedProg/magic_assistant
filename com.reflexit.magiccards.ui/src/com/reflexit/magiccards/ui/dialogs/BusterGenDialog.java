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

import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.reflexit.magiccards.ui.widgets.EditionsComposite;

/**
 * @author Alena
 *
 */
public class BusterGenDialog extends TrayDialog {
	PreferenceStore store = new PreferenceStore();

	/**
	 * @param shell
	 */
	protected BusterGenDialog(Shell shell) {
		super(shell);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		createAmountSelector(area);
		createEditionSelector(area);
		return area;
	}

	/**
	 * @param area 
	 * 
	 */
	private void createAmountSelector(Composite area) {
		// TODO Auto-generated method stub
	}

	/**
	 * @param area 
	 * 
	 */
	private void createEditionSelector(Composite area) {
		EditionsComposite comp = new EditionsComposite(area);
	}
}
