package com.reflexit.magiccards.ui.views.analyzers;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;

public abstract class AbstractDeckListPage extends AbstractDeckPage {
	private AbstractMagicCardsListControl listControl;

	public void createCardsTree(Composite parent) {
		listControl = doGetMagicCardListControl();
		listControl.createPartControl(parent);
		listControl.getManager().getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (!sel.isEmpty()) {
					view.getSite().getSelectionProvider().setSelection(sel);
				}
			}
		});
		hookContextMenu();
	}

	public abstract AbstractMagicCardsListControl doGetMagicCardListControl();

	@Override
	protected void setGlobalControlHandlers(IActionBars bars) {
		super.setGlobalControlHandlers(bars);
		listControl.setGlobalControlHandlers(bars);
	}

	@Override
	protected MenuManager hookContextMenu() {
		MenuManager menuMgr = super.hookContextMenu();
		Control control = listControl.getManager().getControl();
		Menu menu = menuMgr.createContextMenu(control);
		control.setMenu(menu);
		return menuMgr;
	}

	@Override
	protected void fillLocalToolBar(IToolBarManager manager) {
		super.fillLocalToolBar(manager);
		listControl.fillLocalToolBar(manager);
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
		listControl.fillContextMenu(manager);
	}

	public AbstractMagicCardsListControl getListControl() {
		return listControl;
	}
}
