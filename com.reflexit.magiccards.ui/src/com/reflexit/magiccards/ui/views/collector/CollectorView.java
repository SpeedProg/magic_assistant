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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.FilterField;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.CollectorViewPreferencePage;
import com.reflexit.magiccards.ui.views.IViewPage;
import com.reflexit.magiccards.ui.views.ViewPageContribution;
import com.reflexit.magiccards.ui.views.lib.AbstractMyCardsView;

/**
 * Shows sets and how many cards collected per set
 *
 */
public class CollectorView extends AbstractMyCardsView {
	public static final String ID = CollectorView.class.getName();
	private Action refresh;
	private Action onlyOwn;
	private boolean onlyOwnFiltred;
	private CollectorListControl page;

	@Override
	protected void createPages() {
		page = new CollectorListControl();
		getPageGroup().add(new ViewPageContribution("", "Main", null, page));
	}

	@Override
	protected IViewPage getActivePage() {
		return page;
	}

	@Override
	public String getHelpId() {
		return MagicUIActivator.helpId("viewcollector");
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
				DataManager.getInstance().reconcile();
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
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	protected void loadInitialInBackground() {
		reloadData();
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