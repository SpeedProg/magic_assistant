package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeColumn;

import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;
import com.reflexit.magiccards.ui.views.columns.NameColumn;

public class LazyTreeViewerManager extends ViewerManager {
	private TreeViewer viewer;

	public LazyTreeViewerManager(String id) {
		super(id);
	}

	@Override
	public Control createContents(Composite parent) {
		this.viewer = new TreeViewer(parent, SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.MULTI);
		// drillDownAdapter = new DrillDownAdapter(viewer);
		// this.viewer.setContentProvider(new RegularViewContentProvider());
		this.viewer.setContentProvider(new LazyTreeViewContentProvider());
		// this.viewer.setLabelProvider(new MagicCardLabelProvider());
		this.viewer.setUseHashlookup(true);
		updateGrid();
		// viewer.setSorter(new NameSorter());
		createDefaultColumns();
		return this.viewer.getControl();
	}

	protected void createDefaultColumns() {
		getColumnsCollection().createColumnLabelProviders();
		for (int i = 0; i < getColumnsNumber(); i++) {
			AbstractColumn man = getColumn(i);
			TreeViewerColumn colv = new TreeViewerColumn(this.viewer, i);
			TreeColumn col = colv.getColumn();
			col.setText(man.getColumnName());
			col.setWidth(man.getUserWidth());
			col.setToolTipText(man.getColumnTooltip());
			final int coln = i;
			col.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					sortColumn(coln);
				}
			});
			col.setMoveable(true);
			colv.setLabelProvider(man);
			if (man instanceof Listener) {
				this.viewer.getTree().addListener(SWT.PaintItem, (Listener) man);
			}
			colv.setEditingSupport(man.getEditingSupport(this.viewer));
		}
		ColumnViewerToolTipSupport.enableFor(this.viewer, ToolTip.NO_RECREATE);
		this.viewer.getTree().setHeaderVisible(true);
	}

	@Override
	public ColumnViewer getViewer() {
		return this.viewer;
	}

	public void updateColumns(String value) {
		ColumnCollection columnsCollection = getColumnsCollection();
		columnsCollection.updateColumnsFromPropery(value);
		columnsCollection.moveColumnOnTop(columnsCollection.getColumn(GroupColumn.COL_NAME));
		this.viewer.getTree().setColumnOrder(getColumnsCollection().getColumnsOrder());
		TreeColumn[] acolumns = this.viewer.getTree().getColumns();
		for (int i = 0; i < acolumns.length; i++) {
			TreeColumn acol = acolumns[i];
			AbstractColumn mcol = getColumn(i);
			boolean visible = mcol.isVisible();
			if (visible || mcol instanceof GroupColumn) {
				if (acol.getWidth() != mcol.getUserWidth())
					acol.setWidth(mcol.getUserWidth());
			} else {
				acol.setWidth(0);
			}
			// Hide Name column because group column has name
			if (mcol instanceof NameColumn) {
				acol.setWidth(0);
			}
		}
	}

	public String getColumnLayoutProperty() {
		ColumnCollection columnsCollection = getColumnsCollection();
		columnsCollection.setColumnProperties(viewer.getTree().getColumns());
		columnsCollection.setColumnOrder(viewer.getTree().getColumnOrder());
		return columnsCollection.getColumnLayoutProperty();
	}

	public void setLinesVisible(boolean grid) {
		this.viewer.getTree().setLinesVisible(grid);
	}

	public void setSortColumn(int index, int direction) {
		boolean sort = index >= 0;
		if (sort) {
			int sortDirection;
			if (direction == -1)
				sortDirection = SWT.DOWN;
			else if (direction == 0)
				sortDirection = SWT.NONE;
			else
				sortDirection = SWT.UP;
			this.viewer.getTree().setSortDirection(sortDirection);
			TreeColumn column = this.viewer.getTree().getColumn(index);
			this.viewer.getTree().setSortColumn(column);
		} else {
			this.viewer.getTree().setSortColumn(null);
		}
	}

	@Override
	public int getSortDirection() {
		return this.viewer.getTree().getSortDirection();
	}

	protected LazyTreeViewContentProvider getContentProvider() {
		return (LazyTreeViewContentProvider) getViewer().getContentProvider();
	}

	public void updateViewer(Object input) {
		updateTableHeader();
		updateGrid();
		// long time = System.currentTimeMillis();
		// if (this.viewer.getInput() != this.getDataHandler()) {
		if (this.viewer.getInput() != input) {
			this.viewer.setInput(input);
		}
		// System.err.println("set input1 tree time: " +
		// (System.currentTimeMillis() - time) + " ms");
		int size = getContentProvider().getSize(input);
		// System.err.println("size=" + size);
		this.viewer.getTree().setItemCount(size);
		this.viewer.refresh(true);
		// } else {
		// this.viewer.setSelection(new StructuredSelection());
		// this.viewer.getTree().clearAll(true);
		// ((MyTreeViewer) this.viewer).unmapAllElements();
		// this.viewer.refresh(true);
		// }
		// System.err.println("set input2 tree time: " +
		// (System.currentTimeMillis() - time) + " ms");
	}
}
