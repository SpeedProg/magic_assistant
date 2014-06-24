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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.reflexit.mtgtournament.core.edit.CmdCommitRounds;
import com.reflexit.mtgtournament.core.model.Round;
import com.reflexit.mtgtournament.core.model.RoundState;
import com.reflexit.mtgtournament.core.model.TournamentType;
import com.reflexit.mtgtournament.ui.tour.Activator;

/**
 * @author Alena
 * 
 */
public class RoundEditorDialog extends TitleAreaDialog {
	private Round round;
	private Round roundNew;
	private Button reopen;
	private Button sched;
	private Button restart;
	private Combo scheduleCombo;
	private Label endDate;
	private Label startDate;
	private Label state;

	/**
	 * @param parentShell
	 */
	public RoundEditorDialog(Shell parentShell, Round r) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.round = r;
		this.roundNew = new Round(r.getNumber());
		roundNew.copyFrom(r);
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return Activator.getDefault().getDialogSettings(getClass().getSimpleName());
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		parent.getShell().setText("Edit Round");
		setTitle("Round " + round.getNumber());
		Composite comp1 = (Composite) super.createDialogArea(parent);
		Composite comp = new Composite(comp1, SWT.NONE);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		comp.setLayout(new GridLayout(3, false));
		GridDataFactory hor = GridDataFactory.fillDefaults().grab(true, false);
		// state
		Label statusLabel = new Label(comp, SWT.NONE);
		statusLabel.setText("State:");
		state = new Label(comp, SWT.NONE);
		state.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).span(2, 1).create());
		// sched combo
		Label schedule = new Label(comp, SWT.NONE);
		schedule.setText("Schedule:");
		scheduleCombo = new Combo(comp, SWT.READ_ONLY);
		String[] stringValues = TournamentType.stringValues();
		for (String string : stringValues) {
			scheduleCombo.add(string);
		}
		scheduleCombo.setLayoutData(hor.create());
		scheduleCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				roundNew.setType(TournamentType.valueOf(scheduleCombo.getText()));
			}
		});
		scheduleButton(comp);
		// start
		Label startLabel = new Label(comp, SWT.NONE);
		startLabel.setText("Start Time:");
		startDate = new Label(comp, SWT.NONE);
		startDate.setLayoutData(hor.create());
		restartButton(comp);
		// end
		Label endLabel = new Label(comp, SWT.NONE);
		endLabel.setText("End Time:");
		endDate = new Label(comp, SWT.NONE);
		endDate.setLayoutData(hor.create());
		reopenButton(comp);
		// update
		updateButtons();
		return comp;
	}

	private void updateData() {
		state.setText(roundNew.getState().name());
		scheduleCombo.setText(roundNew.getType().name());
		startDate.setText(roundNew.getDateStart() == null ? "" : roundNew.getDateStart().toString());
		endDate.setText(roundNew.getDateEnd() == null ? "" : roundNew.getDateEnd().toString());
	}

	private void reopenButton(Composite comp) {
		reopen = new Button(comp, SWT.PUSH);
		reopen.setText("Re-Open");
		reopen.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (roundNew.getState() == RoundState.CLOSED) {
					roundNew.setDateEnd(null);
				}
				updateButtons();
			}
		});
	}

	private void restartButton(Composite comp) {
		restart = new Button(comp, SWT.PUSH);
		restart.setText("Re-Start");
		restart.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				roundNew.setDateStart(null);
				roundNew.setDateEnd(null);
				updateButtons();
			}
		});
	}

	private void scheduleButton(Composite comp) {
		sched = new Button(comp, SWT.PUSH);
		sched.setText("Re-Schedule");
		sched.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					roundNew.reset();
					roundNew.schedule();
				} catch (Exception e1) {
					MessageDialog.openError(getParentShell(), "Error", e1.getMessage());
				}
				updateButtons();
			}
		});
	}

	/**
	 * 
	 */
	private void updateButtons() {
		updateData();
		scheduleCombo.setEnabled(true);
		sched.setEnabled(true);
		restart.setEnabled(true);
		reopen.setEnabled(true);
		switch (roundNew.getState()) {
			case NOT_READY:
				sched.setEnabled(false);
				restart.setEnabled(false);
				reopen.setEnabled(false);
				break;
			case READY:
				restart.setEnabled(false);
				reopen.setEnabled(false);
				break;
			case NOT_SCHEDULED:
				restart.setEnabled(false);
				reopen.setEnabled(false);
				break;
			case IN_PROGRESS:
				sched.setEnabled(false);
				reopen.setEnabled(false);
				scheduleCombo.setEnabled(false);
				break;
			case CLOSED:
				sched.setEnabled(false);
				scheduleCombo.setEnabled(false);
				break;
		}
	}

	@Override
	protected void okPressed() {
		if (round.getState() == RoundState.CLOSED && roundNew.getState() != RoundState.CLOSED) {
			// undo points for round
			new CmdCommitRounds(round.getTournament(), round.getNumber() - 1).execute();
		}
		round.copyFrom(roundNew);
		super.okPressed();
	}
}
