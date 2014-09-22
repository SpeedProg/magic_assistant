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

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.ViewPart;

import com.reflexit.mtgtournament.core.model.PlayerList;
import com.reflexit.mtgtournament.core.xml.TournamentManager;

public class PlayersView extends ViewPart {
	public static final String ID = PlayersView.class.getName();
	private ScrolledForm form;
	private ISelectionListener selListener;
	private MasterPlayersSection playerListSection;
	private ManagedForm managedForm;
	private PlayerDetailsSection detailsSection;
	private PartListenerAdapter partListener;

	/**
	 * The constructor.
	 */
	public PlayersView() {
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		selListener = new ISelectionListener() {
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				if (part == PlayersView.this) {
					IStructuredSelection iss = (IStructuredSelection) selection;
					if (iss.isEmpty())
						detailsSection.setFormInput(null);
					else
						detailsSection.setFormInput(iss.getFirstElement());
				}
			}
		};
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(ID, selListener);
		partListener = new PartListenerAdapter() {
			@Override
			public void partActivated(IWorkbenchPart part) {
				if (part == PlayersView.this) {
					managedForm.setInput(managedForm.getInput());
					managedForm.reflow(true);
				}
			}
		};
		getSite().getWorkbenchWindow().getPartService().addPartListener(partListener);
	}

	/**
	 * Disposes the toolkit
	 */
	@Override
	public void dispose() {
		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(ID, selListener);
		getSite().getWorkbenchWindow().getPartService().removePartListener(partListener);
		super.dispose();
	}

	protected void setInput(PlayerList playerList) {
		managedForm.setInput(playerList);
		managedForm.reflow(true);
		updateEnablement();
	}

	private void showError(String message) {
		MessageDialog.openError(getViewSite().getShell(), "Error", message);
	}

	/**
	 * This is a callback that will allow us to create the viewer and
	 * initialize it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		managedForm = new ManagedForm(parent) {
			@Override
			public void commit(boolean onSave) {
				Object x = managedForm.getInput();
				if (x instanceof PlayerList) {
					try {
						TournamentManager.save((PlayerList) x);
					} catch (Exception e) {
						showError(e.getMessage());
					}
				}
			}
		};
		form = managedForm.getForm();
		form.setText("All Players");
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;
		form.getBody().setLayout(layout);
		playerListSection = new MasterPlayersSection(managedForm);
		managedForm.addPart(playerListSection);
		detailsSection = new PlayerDetailsSection(managedForm);
		managedForm.addPart(detailsSection);
		// layout
		playerListSection.getSection().setLayoutData(twd(TableWrapData.FILL, 1));
		detailsSection.getSection().setLayoutData(twd(TableWrapData.FILL, 1));
		// sel
		getSite().setSelectionProvider(playerListSection.getSelectionProvider());
		setInitial();
		updateEnablement();
	}

	/**
	 * 
	 */
	private void setInitial() {
		try {
			setInput(TournamentManager.getCube().getPlayerList());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void updateEnablement() {
	}

	private TableWrapData twd(int style, int grab) {
		TableWrapData tableWrapData = new TableWrapData(style);
		tableWrapData.colspan = grab;
		return tableWrapData;
	}

	/**
	 * Passing the focus request to the form.
	 */
	@Override
	public void setFocus() {
		form.setFocus();
	}
}