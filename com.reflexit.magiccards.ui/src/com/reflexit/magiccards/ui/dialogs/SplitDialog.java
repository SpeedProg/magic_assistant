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
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Alena
 * 
 */
public class SplitDialog extends TrayDialog {
	private int split;
	private int max;
	private Button oneToNButton;
	private Button evenButton;
	private Button customButton;
	private Scale scale;

	/**
	 * @param parentShell
	 * @param max
	 */
	protected SplitDialog(Shell parentShell, int max) {
		super(parentShell);
		this.max = max;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Split");
		Composite area = (Composite) super.createDialogArea(parent);
		Composite buttons = new Composite(area, SWT.NONE);
		buttons.setLayout(new GridLayout(2, false));
		GridDataFactory buttonGridData = GridDataFactory.fillDefaults().span(2, 1);
		this.oneToNButton = new Button(buttons, SWT.RADIO);
		this.oneToNButton.setText("1:N");
		buttonGridData.applyTo(this.oneToNButton);
		this.evenButton = new Button(buttons, SWT.RADIO);
		this.evenButton.setText("50/50");
		buttonGridData.applyTo(this.evenButton);
		this.customButton = new Button(buttons, SWT.RADIO);
		this.customButton.setText("Custom");
		final Label from = new Label(buttons, SWT.NONE);
		this.scale = new Scale(buttons, SWT.HORIZONTAL);
		buttonGridData.applyTo(this.scale);
		this.scale.setMinimum(1);
		from.setText("1:" + String.valueOf(this.max - 1));
		this.scale.setMaximum(this.max - 1);
		// scale.add
		this.scale.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selection = SplitDialog.this.scale.getSelection();
				from.setText(String.valueOf(selection) + ":"
						+ String.valueOf(SplitDialog.this.max - selection));
				SplitDialog.this.customButton.setSelection(true);
				SplitDialog.this.evenButton.setSelection(false);
				SplitDialog.this.oneToNButton.setSelection(false);
			}
		});
		this.oneToNButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (SplitDialog.this.oneToNButton.getSelection()) {
					int selection = 1;
					SplitDialog.this.scale.setSelection(selection);
					from.setText(String.valueOf(selection) + ":"
							+ String.valueOf(SplitDialog.this.max - selection));
				}
			}
		});
		this.evenButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (SplitDialog.this.evenButton.getSelection()) {
					int selection = SplitDialog.this.max / 2;
					SplitDialog.this.scale.setSelection(selection);
					from.setText(String.valueOf(selection) + ":"
							+ String.valueOf(SplitDialog.this.max - selection));
				}
			}
		});
		return area;
	}

	@Override
	protected void okPressed() {
		if (this.customButton.getSelection()) {
			this.split = this.scale.getSelection();
		} else if (this.oneToNButton.getSelection()) {
			this.split = 1;
		} else if (this.evenButton.getSelection()) {
			this.split = -2;
		}
		super.okPressed();
	}

	/**
	 * @param shell
	 * @return
	 */
	public static int askSplitType(Shell shell, int max) {
		SplitDialog dialog = new SplitDialog(shell, max);
		if (dialog.open() == dialog.OK) {
			return dialog.getSplit();
		}
		return 0;
	}

	/**
	 * @return
	 */
	private int getSplit() {
		return this.split;
	}
}
