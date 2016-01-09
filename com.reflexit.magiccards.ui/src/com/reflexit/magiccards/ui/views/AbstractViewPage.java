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
package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;

/**
 * AbstractViewPage class
 */
public abstract class AbstractViewPage implements IViewPage {
	private IViewPart view;
	private Composite area;
	private MenuManager menuMgr;

	@Override
	public Composite createContents(Composite parent) {
		return createArea(parent);
	}

	protected Composite createArea(Composite parent) {
		area = new Composite(parent, SWT.NONE);
		area.setLayout(GridLayoutFactory.fillDefaults().create());
		return area;
	}

	@Override
	public Control getControl() {
		return getArea();
	}

	public Composite getArea() {
		return area;
	}

	@Override
	public void activate() {
		IActionBars bars = getViewSite().getActionBars();
		contributeToActionBars(bars);
	}

	public MenuManager getContextMenuManager() {
		return menuMgr;
	}

	public void contributeToActionBars(IActionBars actionBars) {
		// toolbar
		IToolBarManager toolBarManager = actionBars.getToolBarManager();
		toolBarManager.removeAll();
		fillLocalToolBar(toolBarManager);
		toolBarManager.update(true);
		// local view menu
		IMenuManager viewMenuManager = actionBars.getMenuManager();
		viewMenuManager.removeAll();
		fillLocalPullDown(viewMenuManager);
		viewMenuManager.updateAll(true);
		// global handlers
		setGlobalControlHandlers(actionBars);
		actionBars.updateActionBars();
		// context menu
		menuMgr = createContextMenuManager();
		if (hookContextMenu(menuMgr)) {
			getViewSite().registerContextMenu(getViewSite().getId(), menuMgr, getSelectionProvider());
		}
		// selection provider
		getViewSite().setSelectionProvider(getSelectionProvider());
	}

	protected boolean hookContextMenu(MenuManager menuMgr) {
		return false;
	}

	protected IViewSite getViewSite() {
		return view.getViewSite();
	}

	public abstract ISelectionProvider getSelectionProvider();

	/**
	 * @param bars
	 */
	public void setGlobalControlHandlers(IActionBars bars) {
		// override if needed
	}

	protected MenuManager createContextMenuManager() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		return menuMgr;
	}

	/**
	 * @param viewMenuManager
	 */
	public void fillContextMenu(IMenuManager viewMenuManager) {
		// override if need view menu
	}

	/**
	 * @param viewMenuManager
	 */
	public void fillLocalPullDown(IMenuManager viewMenuManager) {
		// override if need view menu
	}

	/**
	 * @param toolBarManager
	 */
	public void fillLocalToolBar(IToolBarManager toolBarManager) {
		// override if need toolbar
	}

	@Override
	public void dispose() {
		area.dispose();
	}

	@Override
	public void init(IViewPart view) {
		this.view = view;
	}

	@Override
	public IViewPart getViewPart() {
		return view;
	}

	@Override
	public void deactivate() {
	}
}
