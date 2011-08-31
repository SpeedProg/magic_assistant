package com.reflexit.magiccards.ui.views;

import java.util.HashMap;
import java.util.HashSet;

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
		// MagicCardLabelProvider labelProvider = new MagicCardLabelProvider();
		// this.viewer.setLabelProvider(labelProvider);
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
			AbstractColumn man = getColumn(i);
			TableViewerColumn colv = new TableViewerColumn(this.viewer, i);
			TableColumn col = colv.getColumn();
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

	@Override
	public ColumnViewer getViewer() {
		return this.viewer;
	}

	public void updateColumns(String newValue) {
		TableColumn[] acolumns = this.viewer.getTable().getColumns();
		int order[] = new int[acolumns.length];
		String[] prefValues = newValue.split(",");
		if (prefValues.length == 0)
			return;
		HashMap<String, Integer> colOrder = new HashMap<String, Integer>();
		HashSet<Integer> orderGaps = new HashSet<Integer>();
		for (int i = 0; i < prefValues.length; i++) {
			Integer integer = Integer.valueOf(i);
			colOrder.put(prefValues[i], integer);
		}
		for (int i = 0; i < order.length; i++) {
			order[i] = -1;
		}
		for (int i = 0; i < acolumns.length; i++) {
			TableColumn acol = acolumns[i];
			AbstractColumn mcol = getColumn(i);
			boolean checked = true;
			String key = mcol.getColumnFullName();
			Integer pos = colOrder.get(key);
			if (pos == null) {
				pos = colOrder.get("-" + key);
				if (pos != null)
					checked = false;
			}
			if (pos != null) {
				order[pos.intValue()] = i;
			} else {
				orderGaps.add(Integer.valueOf(i)); // i'th column has no
													// position
			}
			if (checked && !(mcol instanceof GroupColumn)) {
				if (acol.getWidth() <= 0)
					acol.setWidth(getColumn(i).getColumnWidth());
			} else {
				acol.setWidth(0);
			}
		}
		// fill order for columns which were not in the properly list
		for (int i = 0; i < order.length; i++) {
			int pos = order[i];
			if (pos < 0)
				if (orderGaps.size() > 0) {
					Integer next = orderGaps.iterator().next();
					orderGaps.remove(next);
					order[i] = next.intValue();
				}
		}
		this.viewer.getTable().setColumnOrder(order);
	}

	public void setLinesVisible(boolean grid) {
		this.viewer.getTable().setLinesVisible(grid);
	}

	public void updateSortColumn(int index) {
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
		}
	}

	@Override
	protected int getSortDirection() {
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
