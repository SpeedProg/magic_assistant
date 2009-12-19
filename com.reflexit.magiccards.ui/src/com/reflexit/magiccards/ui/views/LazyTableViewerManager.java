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
import org.eclipse.ui.services.IDisposable;

import java.util.HashMap;
import java.util.HashSet;

import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;

public class LazyTableViewerManager extends ViewerManager implements IDisposable {
	private TableViewer viewer;

	public LazyTableViewerManager(AbstractCardsView view) {
		super(view.doGetFilteredStore(), view.getPreferenceStore(), view.getViewSite().getId());
		this.view = view;
	}

	public LazyTableViewerManager(IFilteredCardStore fs, AbstractCardsView view) {
		super(fs, view.getPreferenceStore(), view.getViewSite().getId());
		this.view = view;
	}

	@Override
	public ColumnViewer getViewer() {
		return this.viewer;
	}
	static class MyTableViewer extends TableViewer {
		public MyTableViewer(Composite parent, int style) {
			super(parent, style);
		}

		@Override
		public void unmapAllElements() {
			super.unmapAllElements();
		}
	}

	@Override
	public Control createContents(Composite parent) {
		this.viewer = new MyTableViewer(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.VIRTUAL);
		// drillDownAdapter = new DrillDownAdapter(viewer);
		// this.viewer.setContentProvider(new RegularViewContentProvider());
		this.viewer.setContentProvider(new LazyTableViewContentProvider());
		//MagicCardLabelProvider labelProvider = new MagicCardLabelProvider();
		//this.viewer.setLabelProvider(labelProvider);
		this.viewer.setUseHashlookup(true);
		addDargAndDrop();
		// viewer.setSorter(new NameSorter());
		createDefaultColumns();
		return this.viewer.getControl();
	}

	protected void createDefaultColumns() {
		createColumnLabelProviders();
		for (int i = 0; i < getColumnsNumber(); i++) {
			AbstractColumn man = (AbstractColumn) this.columns.get(i);
			TableViewerColumn colv = new TableViewerColumn(this.viewer, i);
			TableColumn col = colv.getColumn();
			col.setText(man.getColumnName());
			col.setWidth(man.getColumnWidth());
			col.setToolTipText(man.getColumnTooltip());
			final int coln = i;
			col.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					sort(coln);
				}
			});
			col.setMoveable(true);
			colv.setLabelProvider(man);
			if (man instanceof Listener) {
				this.viewer.getTable().addListener(SWT.PaintItem, (Listener) man);
			}
			;
			colv.setEditingSupport(man.getEditingSupport(this.viewer));
		}
		ColumnViewerToolTipSupport.enableFor(this.viewer, ToolTip.NO_RECREATE);
		this.viewer.getTable().setHeaderVisible(true);
	}

	@Override
	public void updateSortColumn(int index) {
		boolean sort = index >= 0;
		TableColumn column = sort ? this.viewer.getTable().getColumn(index) : null;
		this.viewer.getTable().setSortColumn(column);
		if (sort) {
			int sortDirection = this.viewer.getTable().getSortDirection();
			if (sortDirection != SWT.DOWN)
				sortDirection = SWT.DOWN;
			else
				sortDirection = SWT.UP;
			this.viewer.getTable().setSortDirection(sortDirection);
			this.filter.setAscending(sortDirection == SWT.UP);
			AbstractColumn man = (AbstractColumn) this.viewer.getLabelProvider(index);
			this.filter.setSortField(man.getSortField());
		} else {
			this.filter.setSortField(null);
		}
	}

	@Override
	public void updateViewer() {
		updateTableHeader();
		long time = System.currentTimeMillis();
		if (this.viewer.getInput() != getFilteredStore()) {
			this.viewer.setInput(getFilteredStore());
			this.viewer.setItemCount(getFilteredStore().getSize());
		} else {
			this.viewer.setSelection(new StructuredSelection());
			this.viewer.getTable().clearAll();
			((MyTableViewer) this.viewer).unmapAllElements();
			this.viewer.setItemCount(getFilteredStore().getSize());
			this.viewer.refresh(true);
		}
		updateStatus();
		//System.err.println("set input time: " + (System.currentTimeMillis() - time) + " ms");
	}

	@Override
	public void updateColumns(String newValue) {
		TableColumn[] acolumns = this.viewer.getTable().getColumns();
		int order[] = new int[acolumns.length];
		String[] prefValues = newValue.split(",");
		if (prefValues.length == 0)
			return;
		HashMap<String, Integer> colOrder = new HashMap();
		HashSet<Integer> orderGaps = new HashSet();
		for (int i = 0; i < prefValues.length; i++) {
			Integer integer = Integer.valueOf(i);
			colOrder.put(prefValues[i], integer);
		}
		for (int i = 0; i < order.length; i++) {
			order[i] = -1;
		}
		for (int i = 0; i < acolumns.length; i++) {
			TableColumn acol = acolumns[i];
			AbstractColumn mcol = (AbstractColumn) columns.get(i);
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
				orderGaps.add(Integer.valueOf(i)); // i'th column has no position
			}
			if (checked) {
				if (acol.getWidth() <= 0)
					acol.setWidth(((AbstractColumn) this.columns.get(i)).getColumnWidth());
			} else {
				acol.setWidth(0);
			}
		}
		//fill order for columns which were not in the properly list
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
}
