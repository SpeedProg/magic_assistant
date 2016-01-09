package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;

public interface IMagicControl {
	public abstract Control createPartControl(Composite parent);

	public abstract void dispose();

	public abstract boolean hookContextMenu(MenuManager menuMgr);

	public abstract void init(IViewSite site);

	public abstract ISelectionProvider getSelectionProvider();

	public abstract void setGlobalControlHandlers(IActionBars bars);

	public abstract void fillLocalPullDown(IMenuManager manager);

	public abstract void fillContextMenu(IMenuManager manager);

	public abstract void fillLocalToolBar(IToolBarManager manager);

	public abstract void refresh();

	public abstract void runCopy();

	public abstract void runPaste();

	public abstract Control getControl();

	public abstract void updateViewer();

	public abstract void reloadData();

	public abstract void setStatus(String string);
}