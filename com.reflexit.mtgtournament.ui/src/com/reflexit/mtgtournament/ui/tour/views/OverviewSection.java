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
package com.reflexit.mtgtournament.ui.tour.views;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.Section;

import com.reflexit.mtgtournament.core.edit.CmdCommitTournament;
import com.reflexit.mtgtournament.core.model.Round;
import com.reflexit.mtgtournament.core.model.Tournament;
import com.reflexit.mtgtournament.core.model.TournamentType;

public class OverviewSection extends TSectionPart {
	private static final String SCHEDULE_ACTION_TEXT = "Schedule";
	private static final String UNDO_CLOSE_TOUR_ACTION_TEXT = "Re-open";
	private static final String CLOSE_TOUR_ACTION_TEXT = "Close";
	private static final String RE_SCHEDULE_ACTION_TEXT = "Reset";
	//
	private Combo roundsCombo;
	private Button hasDraftButton;
	private Combo tournamentTypeCombo;
	private Tournament tournament;
	private Button scheduleButton;
	private Button closeButton;
	private PanelAction scheduleAction;
	private PanelAction reScheduleAction;
	private PanelAction closeTourAction;
	private PanelAction undoCloseTourAction;
	private PanelAction updateTourAction;
	private Label statusText;
	private Button reScheduleButton;
	private Button undoCloseButton;
	private Combo opponents;

	public OverviewSection(IManagedForm managedForm) {
		super(managedForm, Section.EXPANDED);
		makeActions();
		createBody();
	}

	private void createBody() {
		Section section = this.getSection();
		section.setText("Overview");
		section.setDescription("Tournament settings");
		Composite sectionClient = toolkit.createComposite(section);
		section.setClient(sectionClient);
		GridLayout layout = new GridLayout(2, false);
		sectionClient.setLayout(layout);
		Composite settings = createSettingsComposite(sectionClient);
		settings.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).create());
		Composite buttons = createButtonsComposite(sectionClient);
		buttons.setLayoutData(GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).create());
	}

	private Composite createSettingsComposite(Composite comp1) {
		Composite comp = new Composite(comp1, SWT.BORDER);
		comp.setLayout(new GridLayout(2, false));
		// tour type
		toolkit.createLabel(comp, "Scheduling type: ");
		tournamentTypeCombo = new Combo(comp, SWT.FLAT | SWT.READ_ONLY);
		toolkit.adapt(tournamentTypeCombo, true, true);
		TournamentType[] values = TournamentType.values();
		for (TournamentType tournamentType : values) {
			tournamentTypeCombo.add(tournamentType.name());
		}
		//tournamentTypeCombo.setText(TournamentType.SWISS.name());
		updateTourAction.attach(tournamentTypeCombo);
		// rounds combo
		toolkit.createLabel(comp, "Total Rounds: ");
		roundsCombo = new Combo(comp, SWT.FLAT);
		toolkit.adapt(roundsCombo, true, true);
		for (int i = 1; i <= 9; i++) {
			roundsCombo.add(String.valueOf(i));
		}
		updateTourAction.attach(roundsCombo);
		GridDataFactory span2 = GridDataFactory.swtDefaults().span(2, 1).align(SWT.FILL, SWT.FILL);
		// has draft check
		hasDraftButton = toolkit.createButton(comp, "Has a draft round", SWT.CHECK);
		hasDraftButton.setLayoutData(span2.create());
		updateTourAction.attach(hasDraftButton, false);
		// opponents
		toolkit.createLabel(comp, "Opponents per game: ");
		opponents = new Combo(comp, SWT.FLAT | SWT.READ_ONLY);
		for (int i = 2; i <= 6; i++) {
			opponents.add(String.valueOf(i));
		}
		updateTourAction.attach(opponents);
		toolkit.adapt(opponents, true, true);
		// status
		statusText = toolkit.createLabel(comp, ".");
		statusText.setLayoutData(span2.create());
		return comp;
	}

	private Composite createButtonsComposite(Composite comp1) {
		Composite comp = new Composite(comp1, SWT.NONE);
		comp.setLayout(new GridLayout(1, true));
		// other actions
		GridDataFactory fill = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL);
		scheduleButton = toolkit.createButton(comp, SCHEDULE_ACTION_TEXT, SWT.PUSH);
		reScheduleButton = toolkit.createButton(comp, RE_SCHEDULE_ACTION_TEXT, SWT.PUSH);
		closeButton = toolkit.createButton(comp, CLOSE_TOUR_ACTION_TEXT, SWT.PUSH);
		undoCloseButton = toolkit.createButton(comp, UNDO_CLOSE_TOUR_ACTION_TEXT, SWT.PUSH);
		// actions
		scheduleAction.attach(scheduleButton, true);
		reScheduleAction.attach(reScheduleButton, true);
		closeTourAction.attach(closeButton, true);
		undoCloseTourAction.attach(undoCloseButton, true);
		// tooltips
		scheduleButton.setToolTipText("Schedule the tournament - creates rounds, and schedules if possible");
		closeButton.setToolTipText("Close the tournament and propagate score");
		reScheduleButton
				.setToolTipText("Clear all the data and resets the tournament to initial state (not scheduled)");
		undoCloseButton.setToolTipText("Undo score propagation and re-open the tournament for modifications");
		// layout
		scheduleButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		reScheduleButton.setLayoutData(fill.create());
		closeButton.setLayoutData(fill.create());
		undoCloseButton.setLayoutData(fill.create());
		return comp;
	}

	private boolean initializing = false;

	/**
	 *
	 */
	protected void makeActions() {
		updateTourAction = new PanelAction() {
			@Override
			protected boolean execute() {
				if (!initializing) {
					updateTournament();
					return true;
				}
				return false;
			}
		};
		scheduleAction = new PanelAction(SCHEDULE_ACTION_TEXT) {
			@Override
			protected boolean execute() {
				tournament.schedule();
				return true;
			}
		};
		reScheduleAction = new PanelAction(RE_SCHEDULE_ACTION_TEXT) {
			@Override
			protected boolean execute() {
				boolean openQuestion = MessageDialog.openQuestion(getSection().getShell(), "Confirmation",
						"This action would erase all rounds and players standings in the tournament."
								+ "Do you want to proceed?");
				if (openQuestion) {
					tournament.setScheduled(false);
				}
				return true;
			}
		};
		closeTourAction = new PanelAction(CLOSE_TOUR_ACTION_TEXT) {
			@Override
			protected boolean execute() {
				boolean openQuestion = MessageDialog.openQuestion(getSection().getShell(), "Confirmation",
						"This action would terminate all unfinished rounds "
								+ "and propagate tournament score table into players score table. "
								+ "Do you want to proceed?");
				if (openQuestion) {
					List<Round> rounds = tournament.getRounds();
					for (Object element : rounds) {
						Round round = (Round) element;
						round.close();
					}
					new CmdCommitTournament(tournament).execute();
					return true;
				}
				return false;
			}
		};
		undoCloseTourAction = new PanelAction(UNDO_CLOSE_TOUR_ACTION_TEXT) {
			@Override
			protected boolean execute() {
				new CmdCommitTournament(tournament).undo();
				return true;
			}
		};
	}

	protected void updateButtonsEnablement() {
		scheduleButton.setEnabled(false);
		reScheduleButton.setEnabled(false);
		closeButton.setEnabled(false);
		undoCloseButton.setEnabled(false);
		if (tournament == null || !tournament.isScheduled()) {
			statusText.setText("Tournament is not started.");
			scheduleButton.setEnabled(true);
		} else if (!tournament.isClosed()) {
			statusText.setText("Tournament has started.");
			closeButton.setEnabled(true);
			reScheduleButton.setEnabled(true);
		} else {
			statusText.setText("Tournament is closed.");
			undoCloseButton.setEnabled(true);
		}
		scheduleButton.getParent().layout(true);
	}

	protected boolean hasDraft() {
		return hasDraftButton.getSelection();
	}

	protected int getRounds() {
		String text = roundsCombo.getText();
		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException e) {
			// TODO set error
			return 4;
		}
	}

	@Override
	public boolean setFormInput(Object input) {
		if (input instanceof Tournament) {
			this.tournament = (Tournament) input;
			initializing = true;
			try {
				hasDraftButton.setSelection(tournament.hasDraftRound());
				tournamentTypeCombo.setText(tournament.getType().name());
				int rounds = tournament.getNumberOfRounds();
				roundsCombo.setText(String.valueOf(rounds));
				opponents.setText(String.valueOf(tournament.getOpponentsPerGame()));
			} finally {
				initializing = false;
			}
			updateEnablement();
		}
		return super.setFormInput(input);
	}

	protected void updateEnablement() {
		boolean vis = !tournament.isScheduled();
		hasDraftButton.setEnabled(vis);
		tournamentTypeCombo.setEnabled(vis);
		roundsCombo.setEnabled(vis);
		opponents.setEnabled(vis);
		updateButtonsEnablement();
	}

	protected TournamentType getType() {
		return TournamentType.valueOf(tournamentTypeCombo.getText());
	}

	protected void updateTournament() {
		try {
			int newRounds = getRounds();
			tournament.setType(getType());
			tournament.setNumberOfRounds(newRounds);
			tournament.setDraft(hasDraft());
			tournament.setOpponentsPerGame(getOpponentsPerGame());
			save();
		} catch (Exception e) {
			MessageDialog.openError(new Shell(), "Error", e.getMessage());
		}
	}

	public int getOpponentsPerGame() {
		try {
			int getOpponentsPerGame = Integer.parseInt(opponents.getText());
			return getOpponentsPerGame;
		} catch (NumberFormatException e) {
			return 2;
		}
	}
}
