package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;

import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.SortOrder;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;

public class TableViewerManager extends ViewerManager {
	private SortOrderViewerComparator vcomp = new SortOrderViewerComparator();
	protected TableViewer viewer;
	protected SortOrder sortOrder;

	public TableViewerManager(String id) {
		super(id);
		sortOrder = new SortOrder();
	}

	@Override
	public Control createContents(Composite parent) {
		this.viewer = new TableViewer(parent, SWT.FULL_SELECTION | SWT.MULTI);
		this.viewer.getTable().setFont(getFont());
		// drillDownAdapter = new DrillDownAdapter(viewer);
		// this.viewer.setContentProvider(new RegularViewContentProvider());
		this.viewer.setContentProvider(new TableViewerContentProvider());
		// this.viewer.setLabelProvider(new MagicCardLabelProvider());
		this.viewer.setUseHashlookup(true);
		IColumnSortAction sortAction = new IColumnSortAction() {
			public void sort(int i) {
				TableViewerManager.this.updateSortColumn(i);
				viewer.refresh(true);
			}
		};
		hookSortAction(sortAction);
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
			TableViewerColumn colv = new TableViewerColumn(this.viewer, i);
			TableColumn col = colv.getColumn();
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
				this.viewer.getTable().addListener(SWT.PaintItem, (Listener) man);
			}
			colv.setEditingSupport(man.getEditingSupport(this.viewer));
		}
		ColumnViewerToolTipSupport.enableFor(this.viewer, ToolTip.NO_RECREATE);
		this.viewer.getTable().setHeaderVisible(true);
	}

	@Override
	public ColumnViewer getViewer() {
		return this.viewer;
	}

	@Override
	public void updateColumns(String value) {
		if (this.viewer.getTable().isDisposed())
			return;
		getColumnsCollection().updateColumnsFromPropery(value);
		this.viewer.getTable().setColumnOrder(getColumnsCollection().getColumnsOrder());
		TableColumn[] acolumns = this.viewer.getTable().getColumns();
		for (int i = 0; i < acolumns.length; i++) {
			TableColumn acol = acolumns[i];
			AbstractColumn mcol = getColumn(i);
			boolean visible = mcol.isVisible();
			if (visible) {
				if (acol.getWidth() != mcol.getUserWidth())
					acol.setWidth(mcol.getUserWidth());
			} else {
				acol.setWidth(0);
			}
		}
	}

	public String getColumnLayoutProperty() {
		ColumnCollection columnsCollection = getColumnsCollection();
		columnsCollection.setColumnProperties(viewer.getTable().getColumns());
		columnsCollection.setColumnOrder(viewer.getTable().getColumnOrder());
		return columnsCollection.getColumnLayoutProperty();
	}

	public void setLinesVisible(boolean grid) {
		this.viewer.getTable().setLinesVisible(grid);
	}

	protected void updateSortColumn(final int index) {
		if (index >= 0) {
			AbstractColumn man = (AbstractColumn) getViewer().getLabelProvider(index);
			ICardField sortField = man.getSortField();
			if (sortField == null)
				sortField = MagicCardField.NAME;
			boolean acc = true;
			if (sortOrder.isTop(sortField)) {
				boolean oldAcc = sortOrder.isAccending(sortField);
				acc = !oldAcc;
			}
			sortOrder.setSortField(sortField, acc);
			setSortColumn(index, acc ? 1 : -1);
		} else {
			setSortColumn(-1, 0);
			sortOrder.clear();
		}
	}

	@Override
	public void setSortColumn(int index, int direction) {
		boolean sort = index >= 0;
		TableColumn column = sort ? this.viewer.getTable().getColumn(index) : null;
		this.viewer.getTable().setSortColumn(column);
		if (sort) {
			int sortDirection = getSortDirection();
			if (sortDirection != SWT.DOWN)
				sortDirection = SWT.DOWN;
			else
				sortDirection = SWT.UP;
			this.viewer.getTable().setSortDirection(sortDirection);
			AbstractColumn man = (AbstractColumn) this.viewer.getLabelProvider(index);
			vcomp.setOrder(man.getSortField(), sortDirection == SWT.UP);
			this.viewer.setComparator(vcomp);
		} else {
			this.viewer.setComparator(null);
		}
	}

	@Override
	public int getSortDirection() {
		return this.viewer.getTable().getSortDirection();
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
}
