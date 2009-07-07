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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.Section;

import java.util.List;

import com.reflexit.mtgtournament.core.edit.CmdCommitTournament;
import com.reflexit.mtgtournament.core.model.Round;
import com.reflexit.mtgtournament.core.model.Tournament;
import com.reflexit.mtgtournament.core.model.TournamentType;

public class OverviewSection extends TSectionPart {
	private static final String OPTIMAL_ROUNDS = "Optimal";
	private Combo roundsCombo;
	private Button hasDraftButton;
	private Combo tournamentTypeCombo;
	private Tournament tournament;
	private FormText scheduleLink;
	private FormText endLink;

	public OverviewSection(IManagedForm managedForm) {
		super(managedForm, Section.EXPANDED);
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
		// tour type
		Label label1 = toolkit.createLabel(sectionClient, "Scheduling type: ");
		tournamentTypeCombo = new Combo(sectionClient, SWT.FLAT | SWT.READ_ONLY);
		toolkit.adapt(tournamentTypeCombo, true, true);
		TournamentType[] values = TournamentType.values();
		for (TournamentType tournamentType : values) {
			tournamentTypeCombo.add(tournamentType.name());
		}
		tournamentTypeCombo.setText(TournamentType.ROUND_ROBIN.name());
		tournamentTypeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTournament();
			}
		});
		// rounds combo
		Label label = toolkit.createLabel(sectionClient, "Total Rounds: ");
		roundsCombo = new Combo(sectionClient, SWT.FLAT);
		toolkit.adapt(roundsCombo, true, true);
		roundsCombo.add(OPTIMAL_ROUNDS);
		for (int i = 1; i <= 8; i++) {
			roundsCombo.add(String.valueOf(i));
		}
		roundsCombo.setText("6");
		roundsCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTournament();
			}
		});
		roundsCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateTournament();
			}
		});
		// has draft check
		GridDataFactory span2 = GridDataFactory.fillDefaults().span(2, 1);
		hasDraftButton = toolkit.createButton(sectionClient, "Has a draft round", SWT.CHECK);
		hasDraftButton.setLayoutData(span2.create());
		hasDraftButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTournament();
			}
		});
		scheduleLink = createLink(sectionClient);
		scheduleLink.setLayoutData(span2.create());
		scheduleLink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				if (e.getHref().equals("schedule")) {
					try {
						tournament.schedule();
						reload();
					} catch (Exception ex) {
						MessageDialog.openError(new Shell(), "Error", ex.getMessage());
					}
				} else if (e.getHref().equals("unschedule")) {
					try {
						tournament.setScheduled(false);
						reload();
					} catch (Exception ex) {
						MessageDialog.openError(new Shell(), "Error", ex.getMessage());
					}
				}
			}
		});
		endLink = createLink(sectionClient);
		endLink.setLayoutData(span2.create());
		endLink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				if (e.getHref().equals("end")) {
					try {
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
							reload();
						}
					} catch (Exception ex) {
						MessageDialog.openError(new Shell(), "Error", ex.getMessage());
					}
				} else if (e.getHref().equals("undo")) {
					try {
						new CmdCommitTournament(tournament).undo();
						reload();
					} catch (Exception ex) {
						MessageDialog.openError(new Shell(), "Error", ex.getMessage());
					}
				}
			}
		});
	}

	private FormText createLink(Composite sectionClient) {
		FormText formText = toolkit.createFormText(sectionClient, true);
		formText.setWhitespaceNormalized(true);
		//	formText.setImage("image", FormArticlePlugin.getDefault().getImageRegistry().get(FormArticlePlugin.IMG_SAMPLE));
		formText.setColor("header", toolkit.getColors().getColor(IFormColors.TITLE));
		formText.setFont("header", JFaceResources.getHeaderFont());
		formText.setFont("code", JFaceResources.getTextFont());
		return formText;
	}

	protected void updateScheduleLink() {
		String text1 = "<form><p><a href=\"schedule\">Schedule the tournament</a></p></form>";
		String text2 = "<form><p>Tournament is scheduled. <a href=\"unschedule\">Reset.</a></p></form>";
		if (tournament == null || !tournament.isScheduled()) {
			scheduleLink.setText(text1, true, false);
			endLink.setText("", false, false);
		} else {
			if (!tournament.isClosed()) {
				endLink.setText("<form><p><a href=\"end\">End</a> the tournament and propagate score</p></form>", true,
				        false);
				scheduleLink.setText(text2, true, false);
			} else {
				scheduleLink.setText("Tournament is closed.", false, false);
				endLink.setText("<form><p><a href=\"undo\">Undo</a> propagate score</p></form>", true, false);
			}
		}
	}

	protected boolean hasDraft() {
		return hasDraftButton.getSelection();
	}

	protected int getRounds() {
		String text = roundsCombo.getText();
		if (text.equals(OPTIMAL_ROUNDS))
			return 0;
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
			hasDraftButton.setSelection(tournament.isDraftRound());
			tournamentTypeCombo.setText(tournament.getType().name());
			int rounds = tournament.getNumberOfRounds();
			roundsCombo.setText(rounds == 0 ? OPTIMAL_ROUNDS : String.valueOf(rounds));
			updateEnablement();
		}
		return super.setFormInput(input);
	}

	protected void updateEnablement() {
		boolean vis = !tournament.isScheduled();
		hasDraftButton.setEnabled(vis);
		tournamentTypeCombo.setEnabled(vis);
		roundsCombo.setEnabled(vis);
		updateScheduleLink();
	}

	protected TournamentType getType() {
		return TournamentType.valueOf(tournamentTypeCombo.getText());
	}

	protected void updateTournament() {
		try {
			tournament.setType(getType(), getRounds(), hasDraft());
			save();
		} catch (Exception e) {
			MessageDialog.openError(new Shell(), "Error", e.getMessage());
		}
	}
}
