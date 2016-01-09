package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.services.IDisposable;

public interface IMagicViewer extends IDisposable {
	Control getControl();

	void setInput(Object input);

	ISelectionProvider getSelectionProvider();

	public abstract Viewer getViewer();
	/**
	 * @param menuMgr
	 * @return TODO
	 */
	boolean hookContextMenu(MenuManager menuMgr);

	void hookSortAction(IColumnSortAction sortAction);

	void refresh();

	void setLinesVisible(boolean grid);

	void hookDragAndDrop();

	void hookContext(String id);

	@Override
	void dispose();

	void addDoubleClickListener(IDoubleClickListener iDoubleClickListener);
}