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
package com.reflexit.magiccards.ui.views.collector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.FilterField;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.CollectorViewPreferencePage;
import com.reflexit.magiccards.ui.views.lib.AbstractMyCardsView;

/**
 * Shows sets and how many cards collected per set
 * 
 */
public class CollectorView extends AbstractMyCardsView implements ISelectionListener {
	public static final String ID = CollectorView.class.getName();
	private Action refresh;
	private Action onlyOwn;
	private boolean onlyOwnFiltred;

	/**
	 * The constructor.
	 */
	public CollectorView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		loadInitial();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, MagicUIActivator.helpId("viewcollector"));
	}

	@Override
	protected void setGlobalHandlers(IActionBars bars) {
		super.setGlobalHandlers(bars);
	}

	@Override
	protected void fillLocalPullDown(IMenuManager manager) {
		manager.add(refresh);
	}

	@Override
	protected void fillLocalToolBar(IToolBarManager manager) {
		// manager.add(onlyOwn);
		// onlyOwn.setChecked(isOnlyOwn());
		super.fillLocalToolBar(manager);
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		this.refresh = new Action("Refresh", SWT.NONE) {
			{
				setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/clcl16/refresh.gif"));
			}

			@Override
			public void run() {
				DataManager.reconcile();
				reloadData();
			}
		};
		this.onlyOwn = new Action("Show Only Own", IAction.AS_CHECK_BOX) {
			{
				setImageDescriptor(MagicUIActivator.getImageDescriptor("icons/obj16/check16.png"));
			}

			@Override
			public void run() {
				triggerOnlyOwn(!isOnlyOwn());
			}
		};
	}

	protected boolean isOnlyOwn() {
		return onlyOwnFiltred;
	}

	public void triggerOnlyOwn(boolean mode) {
		onlyOwnFiltred = mode;
		onlyOwn.setChecked(mode);
		if (!mode)
			onlyOwn.setToolTipText("Check to show only own cards");
		else
			onlyOwn.setToolTipText("Uncheck to show cards in database which you don't own");
		String id = FilterField.OWNERSHIP.getPrefConstant();
		IPreferenceStore store = getLocalPreferenceStore();
		store.putValue(id, onlyOwnFiltred ? "true" : "");
		reloadData();
	}

	@Override
	protected void removeSelected() {
		// System.err.println("Remove attempt");
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		getSite().getPage().addSelectionListener(this);
	}

	@Override
	public void dispose() {
		getSite().getPage().removeSelectionListener(this);
		super.dispose();
	}

	protected void loadInitial() {
		// TOTO remove?
	}

	public void selectionChanged(IWorkbenchPart part, ISelection sel) {
		// TODO remove?
	}

	@Override
	protected CollectorListControl doGetViewControl() {
		return new CollectorListControl(this);
	}

	@Override
	protected String getPreferencePageId() {
		return CollectorViewPreferencePage.class.getName();
	}

	@Override
	public String getId() {
		return ID;
	}
}