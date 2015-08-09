package com.reflexit.magiccards.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionGroup;

public class CopyPasteActionGroup extends ActionGroup {
	private Action actionCopy;
	private Action actionPaste;
	private ISelectionProvider provider;

	public CopyPasteActionGroup(ISelectionProvider provider) {
		this.provider = provider;
		makeActions();
	}

	public void makeActions() {
		this.actionCopy = new MagicCopyAction(provider);
		this.actionPaste = new MagicPasteAction(provider);
	}

	public void setGlobalControlHandlers(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), actionCopy);
		actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(), actionPaste);
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		setGlobalControlHandlers(actionBars);
		fillToolBar(actionBars.getToolBarManager());
		fillViewMenu(actionBars.getMenuManager());
		actionBars.updateActionBars();
	}

	public void fillToolBar(IToolBarManager toolBar) {
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		menu.add(actionCopy);
		menu.add(actionPaste);
	}

	public void fillViewMenu(IMenuManager menu) {
	}

	@Override
	public void dispose() {
		super.dispose();
	}
}
