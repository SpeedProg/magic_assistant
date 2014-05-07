package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IContentProvider;
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

public class TreeViewerManager extends ViewerManager {
	private SortOrderViewerComparator vcomp = new SortOrderViewerComparator();
	protected TreeViewer viewer;

	public TreeViewerManager(String id) {
		super(id);
	}

	@Override
	public Control createContents(Composite parent) {
		this.viewer = new TreeViewer(parent, SWT.FULL_SELECTION | SWT.MULTI);
		this.viewer.getTree().setFont(getFont());
		// drillDownAdapter = new DrillDownAdapter(viewer);
		// this.viewer.setContentProvider(new RegularViewContentProvider());
		this.viewer.setContentProvider(new TreeViewContentProvider());
		// this.viewer.setLabelProvider(new MagicCardLabelProvider());
		this.viewer.setUseHashlookup(true);
		updateGrid();
		// viewer.setSorter(new NameSorter());
		createDefaultColumns();
		return this.viewer.getControl();
	}

	@Override
	public void dispose() {
		this.viewer.getControl().dispose();
		this.viewer = null;
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
		int[] columnsOrder = getColumnsCollection().getColumnsOrder();
		this.viewer.getTree().setColumnOrder(columnsOrder);
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

	public void setSortColumn2(int index, int direction) {
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
	public void setSortColumn(int index, int direction) {
		boolean sort = index >= 0;
		TreeColumn column = sort ? this.viewer.getTree().getColumn(index) : null;
		this.viewer.getTree().setSortColumn(column);
		if (sort) {
			int sortDirection = getSortDirection();
			if (sortDirection != SWT.DOWN)
				sortDirection = SWT.DOWN;
			else
				sortDirection = SWT.UP;
			this.viewer.getTree().setSortDirection(sortDirection);
			AbstractColumn man = (AbstractColumn) this.viewer.getLabelProvider(index);
			vcomp.setOrder(man.getSortField(), sortDirection == SWT.UP);
			this.viewer.setComparator(vcomp);
		} else {
			this.viewer.setComparator(null);
		}
	}

	@Override
	public int getSortDirection() {
		return this.viewer.getTree().getSortDirection();
	}

	protected IContentProvider getContentProvider() {
		return getViewer().getContentProvider();
	}

	public void updateViewer(Object input) {
		if (viewer == null || this.viewer.getControl().isDisposed())
			return;
		updateTableHeader();
		updateGrid();
		// long time = System.currentTimeMillis();
		// if (this.viewer.getInput() != this.getDataHandler()) {
		// if (this.viewer.getInput() != input)
		{
			this.viewer.setInput(input);
		}
		this.viewer.refresh(true);
	}

	protected void hideColumn(int i, boolean hide, TreeColumn[] acolumns) {
		TreeColumn column = acolumns[i];
		if (hide)
			column.setWidth(0);
		else if (column.getWidth() <= 0) {
			int def = getColumn(i).getColumnWidth();
			column.setWidth(def);
		}
	}
}