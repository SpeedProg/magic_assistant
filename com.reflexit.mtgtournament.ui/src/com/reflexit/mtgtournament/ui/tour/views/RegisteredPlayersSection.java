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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.Section;

import com.reflexit.mtgtournament.core.model.Player;
import com.reflexit.mtgtournament.core.model.PlayerTourInfo;
import com.reflexit.mtgtournament.core.model.Tournament;
import com.reflexit.mtgtournament.ui.tour.dialogs.ByesDialog;
import com.reflexit.mtgtournament.ui.tour.dialogs.SelectPlayerDialog;

public class RegisteredPlayersSection extends TSectionPart {
	private Tournament tournament;
	private PlayersListComposite plComp;
	private Button add;
	private Button gen;
	private Button del;
	private Button byes;

	public RegisteredPlayersSection(ManagedForm managedForm) {
		super(managedForm, Section.EXPANDED);
		createBody();
	}

	private void createBody() {
		Section section = this.getSection();
		section.setText("Registered Players and Tournament Standings");
		// section.setDescription("Tournament settings");
		Composite sectionClient = toolkit.createComposite(section);
		section.setClient(sectionClient);
		GridLayout layout = new GridLayout(2, false);
		sectionClient.setLayout(layout);
		// players table
		plComp = new PlayersListComposite(sectionClient, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER, true);
		plComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		plComp.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtonsEnablement();
			}
		});
		// buttons
		createButtons(sectionClient);
	}

	protected void createButtons(Composite sectionClient) {
		Composite buttons = new Composite(sectionClient, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		buttons.setLayout(layout);
		GridDataFactory buttonLD = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).hint(80, -1);
		add = toolkit.createButton(buttons, "Add...", SWT.PUSH);
		add.setLayoutData(buttonLD.create());
		add.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SelectPlayerDialog dialog = new SelectPlayerDialog(plComp.getShell());
				dialog.setInput(tournament.getCube().getPlayerList());
				if (dialog.open() == Dialog.OK) {
					Collection<Player> players = dialog.getPlayers();
					for (Player player : players) {
						tournament.addPlayer(player);
					}
					modelUpdated();
				}
			}
		});
		gen = toolkit.createButton(buttons, "Generate", SWT.PUSH);
		gen.setLayoutData(buttonLD.create());
		gen.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				InputDialog inputDialog = new InputDialog(plComp.getViewer().getControl().getShell(),
						"Enter Players",
						"Enter number of players to generate", "4", new IInputValidator() {
							@Override
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
					modelUpdated();
				}
			}
		});
		del = toolkit.createButton(buttons, "Remove", SWT.PUSH);
		del.setLayoutData(buttonLD.create());
		del.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = (IStructuredSelection) plComp.getViewer().getSelection();
				deletePlayers(sel.toList());
			}
		});
		byes = toolkit.createButton(buttons, "Byes...", SWT.PUSH);
		byes.setLayoutData(buttonLD.create());
		byes.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = (IStructuredSelection) plComp.getViewer().getSelection();
				if (sel.isEmpty()) {
					MessageDialog.openError(byes.getShell(), "Error", "Player is not selected");
					return;
				}
				new ByesDialog(byes.getShell(), sel).open();
			}
		});
		updateButtonsEnablement();
	}

	protected void deletePlayers(List list) {
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Object object = iterator.next();
			if (object instanceof PlayerTourInfo) {
				tournament.removePlayer((PlayerTourInfo) object);
			}
		}
		modelUpdated();
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
		updateButtonsEnablement();
		return true;
	}

	protected void modelUpdated() {
		save();
		plComp.getViewer().refresh(true);
	}

	protected void updateButtonsEnablement() {
		boolean closed = tournament == null || tournament.isClosed();
		IStructuredSelection sel = (IStructuredSelection) plComp.getViewer().getSelection();
		del.setEnabled(!(sel == null || sel.isEmpty()) && !closed);
		add.setEnabled(!closed);
		gen.setEnabled(!closed);
	}

	/**
	 * @return
	 */
	public ISelectionProvider getSelectionProvider() {
		return plComp.getViewer();
	}
}
