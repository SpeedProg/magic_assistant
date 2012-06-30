package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.StructuredSelection;
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
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.GroupColumn;

public class LazyTableViewerManager extends ViewerManager {
	static class MyTableViewer extends TableViewer {
		public MyTableViewer(Composite parent, int style) {
			super(parent, style);
		}

		@Override
		public void unmapAllElements() {
			super.unmapAllElements();
		}
	}

	private TableViewer viewer;

	public LazyTableViewerManager(String id) {
		super(id);
	}

	@Override
	public Control createContents(Composite parent) {
		this.viewer = new MyTableViewer(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.VIRTUAL);
		// drillDownAdapter = new DrillDownAdapter(viewer);
		// this.viewer.setContentProvider(new RegularViewContentProvider());
		this.viewer.setContentProvider(new LazyTableViewContentProvider());
		this.viewer.setUseHashlookup(true);
		updateGrid();
		// viewer.setSorter(new NameSorter());
		createDefaultColumns();
		return this.viewer.getControl();
	}

	protected LazyTableViewContentProvider getContentProvider() {
		return (LazyTableViewContentProvider) getViewer().getContentProvider();
	}

	protected void createDefaultColumns() {
		getColumnsCollection().createColumnLabelProviders();
		int columnsNumber = getColumnsCollection().getColumnsNumber();
		for (int i = 0; i < columnsNumber; i++) {
			final AbstractColumn man = getColumn(i);
			TableViewerColumn colv = new TableViewerColumn(this.viewer, i);
			final TableColumn col = colv.getColumn();
			col.setText(man.getColumnName());
			col.setWidth(man.getColumnWidth());
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

	public String getColumnLayout() {
		ColumnCollection columnsCollection = getColumnsCollection();
		int[] columnOrder = this.viewer.getTable().getColumnOrder();
		String line = "";
		for (int i = 0; i < columnOrder.length; i++) {
			int index = columnOrder[i];
			AbstractColumn column = columnsCollection.getColumn(index);
			String key = column.getColumnFullName();
			if (column.isHidden()) {
				key = "-" + key;
			}
			line += key;
			if (i + 1 < columnOrder.length)
				line += ",";
		}
		columnsCollection.updateColumnsFromPropery(line);
		return line;
	}

	@Override
	public ColumnViewer getViewer() {
		return this.viewer;
	}

	public void updateColumns(String value) {
		getColumnsCollection().updateColumnsFromPropery(value);
		this.viewer.getTable().setColumnOrder(getColumnsCollection().getColumnsOrder());
		TableColumn[] acolumns = this.viewer.getTable().getColumns();
		for (int i = 0; i < acolumns.length; i++) {
			TableColumn acol = acolumns[i];
			AbstractColumn mcol = getColumn(i);
			boolean visible = !mcol.isHidden();
			if (visible && !(mcol instanceof GroupColumn)) {
				if (acol.getWidth() <= 0)
					acol.setWidth(getColumn(i).getUserWidth());
			} else {
				acol.setWidth(0);
			}
		}
	}

	public void setLinesVisible(boolean grid) {
		this.viewer.getTable().setLinesVisible(grid);
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
			this.viewer.getTable().setSortDirection(sortDirection);
			TableColumn column = viewer.getTable().getColumn(index);
			this.viewer.getTable().setSortColumn(column);
		} else {
			this.viewer.getTable().setSortColumn(null);
		}
	}

	@Override
	public int getSortDirection() {
		return this.viewer.getTable().getSortDirection();
	}

	public void updateViewer(Object filteredStore) {
		if (this.viewer.getControl().isDisposed())
			return;
		updateTableHeader();
		updateGrid();
		long time = System.currentTimeMillis();
		int size = getContentProvider().getSize(filteredStore);
		if (this.viewer.getInput() != filteredStore) {
			this.viewer.setInput(filteredStore);
			this.viewer.setItemCount(filteredStore == null ? 0 : size);
		} else {
			this.viewer.setSelection(new StructuredSelection());
			this.viewer.getTable().clearAll();
			((MyTableViewer) this.viewer).unmapAllElements();
			this.viewer.setItemCount(filteredStore == null ? 0 : size);
			this.viewer.refresh(true);
		}
		// System.err.println("set input time: " + (System.currentTimeMillis() -
		// time) + " ms");
	}
}
