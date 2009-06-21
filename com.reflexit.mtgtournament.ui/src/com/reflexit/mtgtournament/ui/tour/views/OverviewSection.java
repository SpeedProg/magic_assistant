package com.reflexit.mtgtournament.ui.tour.views;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.Section;

import com.reflexit.mtgtournament.core.model.Tournament.TournamentType;

public class OverviewSection extends TSectionPart {
	private Combo roundsCombo;
	private Button hasDraftButton;
	private Combo tournamentTypeCombo;

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
		// rounds combo
		Label label = toolkit.createLabel(sectionClient, "Total Rounds: ");
		roundsCombo = new Combo(sectionClient, SWT.FLAT);
		toolkit.adapt(roundsCombo, true, true);
		roundsCombo.add("Optimal");
		for (int i = 1; i <= 8; i++) {
			roundsCombo.add(String.valueOf(i));
		}
		roundsCombo.setText("6");
		// has draft check
		GridDataFactory span2 = GridDataFactory.fillDefaults().span(2, 1);
		hasDraftButton = toolkit.createButton(sectionClient, "Has a draft round", SWT.CHECK);
		hasDraftButton.setLayoutData(span2.create());
	}
}
