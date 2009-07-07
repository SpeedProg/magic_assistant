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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.reflexit.mtgtournament.core.model.TableInfo;
import com.reflexit.mtgtournament.ui.tour.Activator;

public class GameResultDialog extends TrayDialog {
	private Text win1;
	private Text win2;
	private TableInfo input;
	private int nWin1;
	private int nWin2;
	private boolean drop1;
	private boolean drop2;

	public GameResultDialog(Shell shell) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return Activator.getDefault().getDialogSettings();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		parent.getShell().setText("Game Result");
		Composite comp = (Composite) super.createDialogArea(parent);
		comp.setLayout(new GridLayout(4, false));
		GridDataFactory hor = GridDataFactory.fillDefaults();
		win1 = createLabelText(comp, input.getPlayerInfo(1).getPlayer().getName());
		win1.setLayoutData(hor.create());
		win1.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String text = win1.getText();
				nWin1 = validateNumber(text);
			}
		});
		final Button dropBut1 = createDropButton(comp);
		dropBut1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				drop1 = dropBut1.getSelection();
			}
		});
		win2 = createLabelText(comp, input.getPlayerInfo(2).getPlayer().getName());
		win2.setLayoutData(hor.create());
		win2.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String text = win2.getText();
				nWin2 = validateNumber(text);
			}
		});
		final Button dropBut2 = createDropButton(comp);
		dropBut2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				drop2 = dropBut2.getSelection();
			}
		});
		return comp;
	}

	private Button createDropButton(Composite comp) {
		final Button dropBut1 = new Button(comp, SWT.CHECK);
		dropBut1.setText("Drop?");
		return dropBut1;
	}

	protected int validateNumber(String text) {
		// XXX validation
		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public void setInput(Object input) {
		if (input instanceof TableInfo)
			this.input = (TableInfo) input;
	}

	private Text createLabelText(Composite comp, String string) {
		Label label = new Label(comp, SWT.NONE);
		label.setText(string);
		Label label2 = new Label(comp, SWT.NONE);
		label2.setText("W:");
		Text text = new Text(comp, SWT.BORDER);
		text.setTextLimit(2);
		return text;
	}

	/**
	 * @return the nWin1
	 */
	public int getWin1() {
		return nWin1;
	}

	/**
	 * @return the nWin2
	 */
	public int getWin2() {
		return nWin2;
	}

	/**
	 * @return the drop1
	 */
	public boolean isDrop1() {
		return drop1;
	}

	/**
	 * @return the drop2
	 */
	public boolean isDrop2() {
		return drop2;
	}
}
