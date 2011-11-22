package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.reflexit.magiccards.ui.views.columns.ColumnCollection;

public interface IMagicColumnViewer {
	public abstract ColumnCollection getColumnsCollection();

	public abstract Control getControl();

	/**
	 * @return
	 */
	public abstract ISelectionProvider getSelectionProvider();

	public abstract ColumnViewer getViewer();

	/**
	 * @param menuMgr
	 */
	public abstract void hookContextMenu(MenuManager menuMgr);

	/**
	 * @param doubleClickListener
	 */
	public abstract void hookDoubleClickListener(IDoubleClickListener doubleClickListener);

	public abstract void hookSortAction(IColumnSortAction sortAction);

	public abstract void updateColumns(String preferenceValue);

	public abstract void setSortColumn(int index, int direction);

	public abstract void updateViewer(Object input);

	public abstract void flip(boolean hasGroups);

	public abstract void setLinesVisible(boolean grid);

	public abstract int getSortDirection();

	public abstract Control createContents(Composite comp);

	void hookDragAndDrop();
}