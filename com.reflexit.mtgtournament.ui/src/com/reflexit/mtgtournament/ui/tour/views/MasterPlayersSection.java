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

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelectionProvider;
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

import com.reflexit.mtgtournament.core.model.Player;
import com.reflexit.mtgtournament.core.model.PlayerList;
import com.reflexit.mtgtournament.ui.tour.dialogs.NewPlayerDialog;

public class MasterPlayersSection extends TSectionPart {
	private PlayerList plist;
	private PlayersListComposite plComp;

	public MasterPlayersSection(ManagedForm managedForm) {
		super(managedForm, Section.EXPANDED);
		createBody();
	}

	private void createBody() {
		Section section = this.getSection();
		section.setText("Players Listing");
		// section.setDescription("Tournament settings");
		Composite sectionClient = toolkit.createComposite(section);
		section.setClient(sectionClient);
		GridLayout layout = new GridLayout(2, false);
		sectionClient.setLayout(layout);
		// players table
		plComp = new PlayersListComposite(sectionClient, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER, true);
		plComp.getViewer().getTable().getColumn(2).setWidth(0); // hide place
		plComp.getViewer().getTable().getColumn(4).setWidth(0); // hide stats
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		plComp.setLayoutData(layoutData);
		// buttons
		createButtons(sectionClient);
	}

	protected void createButtons(Composite sectionClient) {
		Composite buttons = new Composite(sectionClient, SWT.NONE);
		buttons.setLayoutData(GridDataFactory.fillDefaults().grab(false, true)
				.align(SWT.CENTER, SWT.BEGINNING).create());
		GridLayout layout = new GridLayout(1, true);
		GridDataFactory hor = GridDataFactory.fillDefaults().grab(true, false);
		buttons.setLayout(layout);
		Button add = toolkit.createButton(buttons, "Add...", SWT.PUSH);
		add.setLayoutData(hor.create());
		add.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				NewPlayerDialog dialog = new NewPlayerDialog(plComp.getShell());
				dialog.setPin(plist.getNewId());
				if (dialog.open() == Dialog.OK) {
					Player player = new Player(dialog.getPin(), dialog.getName());
					plist.addPlayer(player);
					modelUpdated();
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
			}
		});
	}

	protected void deletePlayers(List list) {
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Object object = iterator.next();
			if (object instanceof Player) {
				plist.removePlayer((Player) object);
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
		if (input instanceof PlayerList) {
			this.plist = (PlayerList) input;
			if (plist.size() > 20) {
				plComp.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, 400)
						.create());
			} else {
				plComp.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
			}
			plComp.getViewer().setInput(input);
			getManagedForm().reflow(true);
		}
		return true;
	}

	protected void modelUpdated() {
		save();
		plComp.getViewer().refresh(true);
		getManagedForm().reflow(true);
	}

	/**
	 * @return
	 */
	public ISelectionProvider getSelectionProvider() {
		return plComp.getViewer();
	}
}
