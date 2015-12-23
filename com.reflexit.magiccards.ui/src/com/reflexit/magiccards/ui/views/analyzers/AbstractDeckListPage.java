package com.reflexit.magiccards.ui.views.analyzers;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;

import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;

public abstract class AbstractDeckListPage extends AbstractDeckPage {
	private AbstractMagicCardsListControl listControl;

	public Control createListControl(Composite parent) {
		listControl = doGetMagicCardListControl();
		Control part = listControl.createPartControl(parent);
		return part;
	}

	@Override
	public Composite createContents(Composite parent) {
		Composite area = createArea(parent);
		createListControl(area);
		makeActions();
		return area;
	}

	protected void makeActions() {
		// make page specific action
	}

	public abstract AbstractMagicCardsListControl doGetMagicCardListControl();

	@Override
	public ISelectionProvider getSelectionProvider() {
		return listControl.getSelectionProvider();
	}

	@Override
	public void setGlobalControlHandlers(IActionBars bars) {
		super.setGlobalControlHandlers(bars);
		listControl.setGlobalControlHandlers(bars);
	}

	@Override
	protected MenuManager hookContextMenu() {
		MenuManager menuMgr = super.hookContextMenu();
		listControl.hookContextMenu(menuMgr);
		return menuMgr;
	}

	@Override
	public void fillLocalToolBar(IToolBarManager manager) {
		super.fillLocalToolBar(manager);
		listControl.fillLocalToolBar(manager);
	}

	@Override
	public void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
		listControl.fillContextMenu(manager);
	}

	public AbstractMagicCardsListControl getListControl() {
		return listControl;
	}

	@Override
	public void dispose() {
		getListControl().dispose();
		super.dispose();
	}
}
