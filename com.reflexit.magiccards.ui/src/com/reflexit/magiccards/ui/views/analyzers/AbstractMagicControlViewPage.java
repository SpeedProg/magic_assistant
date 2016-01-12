package com.reflexit.magiccards.ui.views.analyzers;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;

import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.AbstractViewPage;

/**
 * View page that contains a single magicCardListControl
 * 
 * @author elaskavaia
 *
 */
public abstract class AbstractMagicControlViewPage extends AbstractViewPage {
	private AbstractMagicCardsListControl listControl;

	public Control createListControl(Composite parent) {
		listControl = doGetMagicCardListControl();
		Control part = listControl.createPartControl(parent);
		return part;
	}

	@Override
	protected void createPageContents(Composite area) {
		createListControl(area);
		makeActions();
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
	public void setGlobalHandlers(IActionBars bars) {
		super.setGlobalHandlers(bars);
		listControl.setGlobalHandlers(bars);
	}

	@Override
	public boolean hookContextMenu(MenuManager menuMgr) {
		listControl.hookContextMenu(menuMgr);
		return true;
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

	public AbstractMagicCardsListControl getMagicControl() {
		return listControl;
	}

	@Override
	public void dispose() {
		getMagicControl().dispose();
		super.dispose();
	}
}
