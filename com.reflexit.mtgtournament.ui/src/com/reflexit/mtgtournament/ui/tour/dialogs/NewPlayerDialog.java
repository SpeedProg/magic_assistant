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
package com.reflexit.mtgtournament.ui.tour.dialogs;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.reflexit.mtgtournament.ui.tour.Activator;

public class NewPlayerDialog extends TrayDialog {
	private Text pinText;
	private Text nameText;
	private String name;
	private String pin;

	public NewPlayerDialog(Shell shell) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return Activator.getDefault().getDialogSettings();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		parent.getShell().setText("Find Player");
		Composite comp = (Composite) super.createDialogArea(parent);
		comp.setLayout(new GridLayout(2, false));
		GridDataFactory hor = GridDataFactory.fillDefaults().grab(true, false);
		pinText = createLabelText(comp, "PIN:");
		pinText.setLayoutData(hor.create());
		pinText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String text = pinText.getText();
				pin = text;
			}
		});
		nameText = createLabelText(comp, "Name:");
		nameText.setLayoutData(hor.create());
		nameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String text = nameText.getText();
				name = text;
			}
		});
		return comp;
	}

	private Text createLabelText(Composite comp, String string) {
		Label label = new Label(comp, SWT.NONE);
		label.setText(string);
		Text text = new Text(comp, SWT.BORDER);
		return text;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the pin
	 */
	public String getPin() {
		return pin;
	}
}
