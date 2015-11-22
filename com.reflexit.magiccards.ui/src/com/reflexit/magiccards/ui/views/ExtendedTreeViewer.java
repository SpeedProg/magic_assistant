package com.reflexit.magiccards.ui.views;

import java.util.Arrays;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;

public class ExtendedTreeViewer extends TreeViewer implements IMagicColumnViewer {
	protected TreeViewerManager manager;

	protected ExtendedTreeViewer(Composite parent, int style) {
		super(parent, style);
		getControl().setFont(MagicUIActivator.getDefault().getFont());
	}

	protected ExtendedTreeViewer(Composite parent) {
		super(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
	}

	public ExtendedTreeViewer(Composite parent, ColumnCollection collection) {
		this(parent);
		this.manager = new TreeViewerManager(this, collection);
	}

	public ExtendedTreeViewer(Composite parent, String id) {
		this(parent);
		this.manager = new TreeViewerManager(this, doGetColumnCollection(id));
	}

	protected ColumnCollection doGetColumnCollection(String prefPageId) {
		return new MagicColumnCollection(prefPageId);
	}

	@Override
	public void unmapAllElements() {
		super.unmapAllElements();
	}

	@Override
	protected void associate(Object element, Item item) {
		if (element == null)
			return;
		super.associate(element, item);
	}

	@Override
	public ColumnCollection getColumnsCollection() {
		return manager.getColumnsCollection();
	}

	@Override
	public void dispose() {
		manager.dispose();
	}

	@Override
	public void hookContext(String id) {
		manager.hookContext(id);
	}

	protected void hide(final Item column) {
		((TreeColumn) column).setWidth(0);
		((TreeColumn) column).setResizable(false);
	}

	@Override
	public void updateColumns(String value) {
		getColumnsCollection().updateColumnsFromPropery(value);
		syncColumns();
	}

	protected void syncColumns() {
		ColumnCollection columnsCollection = getColumnsCollection();
		columnsCollection.moveColumnOnTop(columnsCollection.getColumn(GroupColumn.COL_NAME));
		int[] columnsOrder = getColumnsCollection().getColumnsOrder();
		if (manager.filler == 1) {
			int length = columnsOrder.length;
			columnsOrder = Arrays.copyOf(columnsOrder, length + 1);
			columnsOrder[length] = length; // last column
		}
		getTControl().setColumnOrder(columnsOrder);
		TreeColumn[] acolumns = getTree().getColumns();
		for (int i = 0; i < acolumns.length - manager.filler; i++) {
			TreeColumn acol = acolumns[i];
			AbstractColumn mcol = manager.getColumn(i);
			boolean visible = mcol.isVisible();
			if (visible || mcol instanceof GroupColumn) {
				int w = mcol.getUserWidth();
				if (w < 16)
					w = 16; // min reasonable width
				if (w > 500)
					w = 500;
				if (acol.getWidth() != w) {
					acol.setWidth(w);
				}
			} else {
				acol.setWidth(0);
			}
		}
	}

	@Override
	public void hookContextMenu(MenuManager menuMgr) {
		manager.hookContextMenu(menuMgr);
	}

	public MenuManager getMenuManager() {
		return manager.getMenuManager();
	}

	@Override
	public void hookSortAction(IColumnSortAction sortAction) {
		manager.hookSortAction(sortAction);
	}

	@Override
	public void refresh() {
		super.refresh();
		if (manager != null)
			manager.updatePresentation();
	}

	@Override
	public void hookDragAndDrop() {
		manager.hookDragAndDrop();
	}

	public void hookDragAndDrop(StructuredViewer viewer) {
		manager.hookDragAndDrop(viewer);
	}

	public Font getFont() {
		return manager.getFont();
	}

	@Override
	public Viewer getViewer() {
		return this;
	}

	@Override
	public void setSortColumn(int index, int direction) {
		manager.setSortColumn(index, direction);
	}

	public SortOrderViewerComparator getViewerComparator() {
		return manager.getViewerComparator();
	}

	@Override
	public String getColumnLayoutProperty() {
		manager.applyColumnProperties();
		ColumnCollection columnsCollection = getColumnsCollection();
		return columnsCollection.getColumnLayoutProperty();
	}

	public void setColumnProperties(TreeColumn[] acolumns) {
		manager.setColumnProperties(acolumns);
	}

	@Override
	public void setLinesVisible(boolean grid) {
		getTControl().setLinesVisible(grid);
	}

	@Override
	public int getSortDirection() {
		return getTControl().getSortDirection();
	}

	protected Tree getTControl() {
		return getTree();
	}

	@Override
	protected void inputChanged(Object input, Object oldInput) {
		super.inputChanged(input, oldInput);
		manager.updatePresentation();
	}

	public boolean supportsGroupping(boolean groupped) {
		if (groupped)
			return true;
		return false;
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return this;
	}

	public StructuredViewer getStructuredViewer() {
		return this;
	}

	@Override
	public ColumnViewer getColumnViewer() {
		return this;
	}
}