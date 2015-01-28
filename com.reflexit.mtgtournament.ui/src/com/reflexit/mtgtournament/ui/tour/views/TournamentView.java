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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.ViewPart;

import com.reflexit.mtgtournament.core.model.Tournament;
import com.reflexit.mtgtournament.core.xml.TournamentManager;

public class TournamentView extends ViewPart {
	public static final String ID = TournamentView.class.getName();
	private ScrolledForm form;
	private ISelectionListener selListener;
	private OverviewSection overviewSectionPart;
	private RegisteredPlayersSection regPlayersSectionPart;
	private ManagedForm managedForm;
	private RoundScheduleSection roundSectionPart;
	private RoundListSection roundListSectionPart;
	private PartListenerAdapter partListener;

	/**
	 * The constructor.
	 */
	public TournamentView() {
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		selListener = new ISelectionListener() {
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				setInput(selection);
			}
		};
		partListener = new PartListenerAdapter() {
			@Override
			public void partActivated(IWorkbenchPart part) {
				if (part == getSite().getPart()) {
					managedForm.setInput(managedForm.getInput());
					managedForm.reflow(true);
				}
			}
		};
		getSite().getWorkbenchWindow().getSelectionService()
				.addSelectionListener(TNavigatorView.ID, selListener);
		getSite().getWorkbenchWindow().getPartService().addPartListener(partListener);
	}

	/**
	 * Disposes the toolkit
	 */
	@Override
	public void dispose() {
		getSite().getWorkbenchWindow().getSelectionService()
				.removeSelectionListener(TNavigatorView.ID, selListener);
		getSite().getWorkbenchWindow().getPartService().removePartListener(partListener);
		super.dispose();
	}

	protected void setInput(ISelection selection) {
		if (selection.isEmpty())
			return;
		if (!(selection instanceof IStructuredSelection))
			return;
		Object x = ((IStructuredSelection) selection).getFirstElement();
		if (x instanceof Tournament) {
			Tournament t = (Tournament) x;
			form.setText("Tournament: " + t.getName());
			managedForm.setInput(t);
			managedForm.reflow(true);
		}
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
				if (x instanceof Tournament) {
					try {
						TournamentManager.save((Tournament) x);
					} catch (Exception e) {
						showError(e.getMessage());
					}
				}
			}
		};
		form = managedForm.getForm();
		form.setText("Tournament (Select a Tournament)");
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;
		form.getBody().setLayout(layout);
		overviewSectionPart = new OverviewSection(managedForm);
		managedForm.addPart(overviewSectionPart);
		roundListSectionPart = new RoundListSection(managedForm);
		managedForm.addPart(roundListSectionPart);
		regPlayersSectionPart = new RegisteredPlayersSection(managedForm);
		managedForm.addPart(regPlayersSectionPart);
		roundSectionPart = new RoundScheduleSection(managedForm);
		managedForm.addPart(roundSectionPart);
		// layout
		overviewSectionPart.getSection().setLayoutData(twd(TableWrapData.FILL, 1));
		roundListSectionPart.getSection().setLayoutData(twd(TableWrapData.FILL, 1));
		regPlayersSectionPart.getSection().setLayoutData(twd(TableWrapData.FILL_GRAB, 2));
		roundSectionPart.getSection().setLayoutData(twd(TableWrapData.FILL_GRAB, 2));
		setInitial();
		updateEnablement();
	}

	/**
	 * 
	 */
	private void setInitial() {
		ISelection selection = getSite().getWorkbenchWindow().getSelectionService()
				.getSelection(TNavigatorView.ID);
		if (selection != null) {
			setInput(selection);
		}
	}

	/**
	 * 
	 */
	private void updateEnablement() {
		boolean enabled = (managedForm.getInput() != null);
		IFormPart[] parts = managedForm.getParts();
		for (IFormPart formPart : parts) {
			((TSectionPart) formPart).getSection().setEnabled(enabled);
		}
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