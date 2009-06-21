package com.reflexit.mtgtournament.ui.tour.views;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.Section;

import java.util.Iterator;
import java.util.List;

import com.reflexit.mtgtournament.core.model.Player;
import com.reflexit.mtgtournament.core.model.Tournament;
import com.reflexit.mtgtournament.ui.tour.dialogs.SelectPlayerDialog;

public class RegisteredPlayersSection extends TSectionPart {
	private Tournament tournament;
	private PlayersListComposite plComp;

	public RegisteredPlayersSection(ManagedForm managedForm) {
		super(managedForm, Section.EXPANDED);
		createBody();
	}

	private void createBody() {
		Section section = this.getSection();
		section.setText("Registered Players");
		//section.setDescription("Tournament settings");
		Composite sectionClient = toolkit.createComposite(section);
		section.setClient(sectionClient);
		GridLayout layout = new GridLayout(2, false);
		sectionClient.setLayout(layout);
		// players table
		plComp = new PlayersListComposite(sectionClient, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER, true);
		plComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		// buttons
		createButtons(sectionClient);
	}

	protected void createButtons(Composite sectionClient) {
		Composite buttons = new Composite(sectionClient, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		GridDataFactory hor = GridDataFactory.fillDefaults().grab(true, false);
		buttons.setLayout(layout);
		Button add = toolkit.createButton(buttons, "Add...", SWT.PUSH);
		add.setLayoutData(hor.create());
		add.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SelectPlayerDialog dialog = new SelectPlayerDialog(plComp.getShell());
				dialog.setInput(tournament.getCube());
				if (dialog.open() == Dialog.OK) {
					Player player = dialog.getPlayer();
					if (player != null) {
						tournament.addPlayer(player);
						plComp.getViewer().refresh(true);
					}
				}
			}
		});
		Button gen = toolkit.createButton(buttons, "Generate...", SWT.PUSH);
		gen.setLayoutData(hor.create());
		gen.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				InputDialog inputDialog = new InputDialog(plComp.getViewer().getControl().getShell(), "Enter Players",
				        "Enter number of players to generate", "4", new IInputValidator() {
					        public String isValid(String newText) {
						        try {
							        int x = Integer.parseInt(newText);
							        if (x <= 0)
								        return "No players?";
						        } catch (NumberFormatException e) {
							        return "Invalid number";
						        }
						        return null;
					        }
				        });
				if (inputDialog.open() == Dialog.OK) {
					String value = inputDialog.getValue();
					int num = Integer.parseInt(value);
					tournament.generatePlayers(num);
					plComp.getViewer().refresh(true);
				}
			}
		});
		Button del = toolkit.createButton(buttons, "Remove", SWT.PUSH);
		del.setLayoutData(hor.create());
		del.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = (IStructuredSelection) plComp.getViewer().getSelection();
				deletePlayers(sel.toList());
				plComp.getViewer().refresh(true);
			}
		});
	}

	protected void deletePlayers(List list) {
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Object object = iterator.next();
			if (object instanceof Player) {
				tournament.removePlayer((Player) object);
			}
		}
	}

	@Override
	public void refresh() {
		plComp.getViewer().refresh(true);
		super.refresh();
	}

	@Override
	public boolean setFormInput(Object input) {
		if (input instanceof Tournament) {
			this.tournament = (Tournament) input;
		}
		plComp.getViewer().setInput(input);
		return true;
	}
}
