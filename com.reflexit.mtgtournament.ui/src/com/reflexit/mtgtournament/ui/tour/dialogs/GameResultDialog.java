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
	private Text win[];
	private TableInfo input;
	private int nWin[];
	private boolean drop[];
	protected int nDraw;
	private Text draw;

	public GameResultDialog(Shell shell) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return Activator.getDefault().getDialogSettings(getClass().getSimpleName());
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		parent.getShell().setText("Game Result");
		Composite comp = (Composite) super.createDialogArea(parent);
		comp.setLayout(new GridLayout(4, false));
		GridDataFactory hor = GridDataFactory.fillDefaults();
		int ops = input.getOpponentsPerGame();
		win = new Text[ops];
		nWin = new int[ops];
		drop = new boolean[ops];
		for (int i = 0; i < ops; i++) {
			final int fi = i;
			final Text win1 = createLabelText(comp, input.getOpponent(i).getPlayer().getName());
			win1.setLayoutData(hor.create());
			win1.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					String text = win1.getText();
					nWin[fi] = validateNumber(text);
				}
			});
			win[i] = win1;
			final Button dropBut1 = createDropButton(comp);
			dropBut1.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					drop[fi] = dropBut1.getSelection();
				}
			});
		}
		Label label = new Label(comp, SWT.NONE);
		label.setText("Draw #");
		Label label2 = new Label(comp, SWT.NONE);
		label2.setText("");
		draw = new Text(comp, SWT.BORDER);
		draw.setTextLimit(2);
		draw.setLayoutData(hor.create());
		draw.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String text = draw.getText();
				nDraw = validateNumber(text);
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
	 * @return the nWin2
	 */
	public int getWin(int playerNumber) {
		return nWin[playerNumber - 1];
	}

	public int getLost(int playerNumber) {
		int sum = 0;
		for (int i = 0; i < nWin.length; i++) {
			if (i != playerNumber - 1) sum += nWin[i];
		}
		return sum;
	}

	public int getDraw() {
		return nDraw;
	}

	/**
	 * @return the drop1
	 */
	public boolean isDrop(int playerNumber) {
		return drop[playerNumber - 1];
	}
}
