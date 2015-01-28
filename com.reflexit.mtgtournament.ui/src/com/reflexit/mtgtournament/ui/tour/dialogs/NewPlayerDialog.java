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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.reflexit.mtgtournament.ui.tour.Activator;

public class NewPlayerDialog extends TitleAreaDialog {
	private Text pinText;
	private Text nameText;
	private String name = "";
	private String pin = "";

	public NewPlayerDialog(Shell shell) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return Activator.getDefault().getDialogSettings(getClass().getSimpleName());
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		parent.getShell().setText("New Player");
		setTitle("Add a new player");
		setMessage("Enter player's name and PIN (Player Identification Number). PIN must be uniqueue.");
		Composite comp1 = (Composite) super.createDialogArea(parent);
		Composite comp = new Composite(comp1, SWT.NONE);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		comp.setLayout(new GridLayout(2, false));
		GridDataFactory hor = GridDataFactory.fillDefaults().grab(true, false);
		nameText = createLabelText(comp, "Name:");
		nameText.setLayoutData(hor.create());
		nameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String text = nameText.getText();
				name = text;
				validate();
			}
		});
		pinText = createLabelText(comp, "PIN:");
		pinText.setLayoutData(hor.create());
		pinText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String text = pinText.getText();
				pin = text;
				validate();
			}
		});
		pinText.setText(pin);
		return comp1;
	}

	@Override
	protected Control createContents(Composite parent) {
		Control x = super.createContents(parent);
		setErrorMessage(null);
		getButton(IDialogConstants.OK_ID).setEnabled(false); // name is not entered
		return x;
	}

	/**
	 * 
	 */
	protected void validate() {
		boolean error = false;
		if (pin.length() == 0) {
			setErrorMessage("Player's PID (id) cannot be empty. Enter any unique id.");
			error = true;
		} else if (name.length() == 0) {
			setErrorMessage("Player's name cannot be empty");
			error = true;
		}
		if (!error)
			setErrorMessage(null);
		Button button = getButton(IDialogConstants.OK_ID);
		if (button != null)
			button.setEnabled(!error);
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

	/**
	 * sets default pin (id)
	 * 
	 * @param pin
	 */
	public void setPin(String pin) {
		this.pin = pin;
	}
}
