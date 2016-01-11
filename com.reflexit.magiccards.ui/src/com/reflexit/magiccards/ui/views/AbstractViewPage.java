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
	public Control createContents(Composite parent) {
		return createArea(parent);
	}

	public final Composite createArea(Composite parent) {
		if (area == null || !area.isDisposed()) {
			area = new Composite(parent, SWT.NONE);
			area.setLayout(GridLayoutFactory.fillDefaults().create());
		}
		return area;
	}

	public void setPlaceholder(Control placeholder) {
		if (placeholder instanceof Composite)
			area = (Composite) placeholder;
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
		contributeToActionBars();
		// selection provider
		// getViewSite().setSelectionProvider(getSelectionProvider());
	}

	public void contributeToActionBars() {
		IActionBars actionBars = getViewSite().getActionBars();
		// toolbar
		IToolBarManager toolBarManager = actionBars.getToolBarManager();
		// toolBarManager.removeAll();
		fillLocalToolBar(toolBarManager);
		toolBarManager.update(true);
		// local view menu
		IMenuManager viewMenuManager = actionBars.getMenuManager();
		// viewMenuManager.removeAll();
		fillLocalPullDown(viewMenuManager);
		viewMenuManager.updateAll(true);
		// global handlers
		setGlobalHandlers(actionBars);
		actionBars.updateActionBars();
		// context menu
		hookContextMenu();
	}

	protected void hookContextMenu() {
		// context menu
		if (menuMgr == null)
			menuMgr = createContextMenuManager();
		hookContextMenu(menuMgr);
	}

	public boolean hookContextMenu(MenuManager menuMgr) {
		return false;
	}

	protected IViewSite getViewSite() {
		return view.getViewSite();
	}

	@Override
	public abstract ISelectionProvider getSelectionProvider();

	/**
	 * @param bars
	 */
	public void setGlobalHandlers(IActionBars bars) {
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

	@Override
	public MenuManager getContextMenuManager() {
		return menuMgr;
	}

	@Override
	public void setContextMenuManager(MenuManager menuMgr) {
		this.menuMgr = menuMgr;
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
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
