package com.reflexit.mtgtournament.ui.tour.views;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
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

import com.reflexit.mtgtournament.core.model.Tournament;
import com.reflexit.mtgtournament.core.model.Tournament.TournamentType;

public class OverviewSection extends TSectionPart {
	private static final String OPTIMAL_ROUNDS = "Optimal";
	private Combo roundsCombo;
	private Button hasDraftButton;
	private Combo tournamentTypeCombo;
	private Tournament tournament;
	private FormText scheduleLink;

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
		scheduleLink = createScheduleLink(sectionClient);
		scheduleLink.setLayoutData(span2.create());
	}

	private FormText createScheduleLink(Composite sectionClient) {
		FormText formText = toolkit.createFormText(sectionClient, true);
		formText.setWhitespaceNormalized(true);
		//	formText.setImage("image", FormArticlePlugin.getDefault().getImageRegistry().get(FormArticlePlugin.IMG_SAMPLE));
		formText.setColor("header", toolkit.getColors().getColor(IFormColors.TITLE));
		formText.setFont("header", JFaceResources.getHeaderFont());
		formText.setFont("code", JFaceResources.getTextFont());
		formText.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				if (e.getHref().equals("schedule")) {
					try {
						tournament.schedule();
						getManagedForm().setInput(tournament);
						getManagedForm().refresh();
					} catch (Exception ex) {
						MessageDialog.openError(new Shell(), "Error", ex.getMessage());
					}
				} else if (e.getHref().equals("unschedule")) {
					try {
						tournament.setScheduled(false);
						getManagedForm().setInput(tournament);
						getManagedForm().refresh();
					} catch (Exception ex) {
						MessageDialog.openError(new Shell(), "Error", ex.getMessage());
					}
				}
			}
		});
		return formText;
	}

	protected void updateScheduleLink() {
		String text1 = "<form><p><a href=\"schedule\">Schedule the tournament</a></p></form>";
		String text2 = "<form><p>Tournament is scheduled. <a href=\"unschedule\">Reset.</a></p></form>";
		if (tournament == null || !tournament.isScheduled())
			scheduleLink.setText(text1, true, false);
		else
			scheduleLink.setText(text2, true, false);
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
		} catch (Exception e) {
			MessageDialog.openError(new Shell(), "Error", e.getMessage());
		}
	}
}
