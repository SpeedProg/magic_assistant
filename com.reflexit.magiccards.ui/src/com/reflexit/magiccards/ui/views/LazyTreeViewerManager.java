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
import org.eclipse.ui.services.IDisposable;

import java.util.HashMap;
import java.util.HashSet;

import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.views.columns.AbstractColumn;

public class LazyTreeViewerManager extends ViewerManager implements IDisposable {
	private MyTreeViewer viewer;

	LazyTreeViewerManager(AbstractCardsView view) {
		super(view.doGetFilteredStore(), view.getPreferenceStore(), view.getViewSite().getId());
		this.view = view;
	}

	/**
	 * @param filteredStore
	 * @param view
	 */
	public LazyTreeViewerManager(IFilteredCardStore filteredStore, AbstractCardsView view) {
		super(filteredStore, view.getPreferenceStore(), view.getViewSite().getId());
		this.view = view;
	}

	@Override
	public ColumnViewer getViewer() {
		return this.viewer;
	}
	static class MyTreeViewer extends TreeViewer {
		public MyTreeViewer(Composite parent, int style) {
			super(parent, style);
		}

		@Override
		public void unmapAllElements() {
			super.unmapAllElements();
		}
	}

	@Override
	public Control createContents(Composite parent) {
		this.viewer = new MyTreeViewer(parent, SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.MULTI);
		// drillDownAdapter = new DrillDownAdapter(viewer);
		// this.viewer.setContentProvider(new RegularViewContentProvider());
		this.viewer.setContentProvider(new LazyTreeViewContentProvider());
		//	this.viewer.setLabelProvider(new MagicCardLabelProvider());
		this.viewer.setUseHashlookup(true);
		// viewer.setSorter(new NameSorter());
		addDargAndDrop();
		createDefaultColumns();
		return this.viewer.getControl();
	}

	@Override
	public void updateSortColumn(int index) {
		boolean sort = index >= 0;
		TreeColumn column = sort ? this.viewer.getTree().getColumn(index) : null;
		this.viewer.getTree().setSortColumn(column);
		if (sort) {
			int sortDirection = this.viewer.getTree().getSortDirection();
			if (sortDirection != SWT.DOWN)
				sortDirection = SWT.DOWN;
			else
				sortDirection = SWT.UP;
			this.viewer.getTree().setSortDirection(sortDirection);
			this.filter.setAscending(sortDirection == SWT.UP);
			AbstractColumn man = (AbstractColumn) this.viewer.getLabelProvider(index);
			this.filter.setSortField(man.getSortField());
		} else {
			this.filter.setSortField(null);
		}
	}

	@Override
	protected void updateViewer() {
		updateTableHeader();
		long time = System.currentTimeMillis();
		//	if (this.viewer.getInput() != this.getDataHandler()) {
		this.viewer.unmapAllElements();
		this.viewer.setInput(this.getFilteredStore());
		//System.err.println("set input1 tree time: " + (System.currentTimeMillis() - time) + " ms");
		int size = this.getFilteredStore().getCardGroups().length;
		//System.err.println("size=" + size);
		this.viewer.getTree().setItemCount(size);
		//		} else {
		//			this.viewer.setSelection(new StructuredSelection());
		//			this.viewer.getTree().clearAll(true);
		//			((MyTreeViewer) this.viewer).unmapAllElements();
		//			this.viewer.refresh(true);
		//		}
		//System.err.println("set input2 tree time: " + (System.currentTimeMillis() - time) + " ms");
		updateStatus();
	}

	protected void createDefaultColumns() {
		createColumnLabelProviders();
		for (int i = 0; i < getColumnsNumber(); i++) {
			AbstractColumn man = (AbstractColumn) this.columns.get(i);
			TreeViewerColumn colv = new TreeViewerColumn(this.viewer, i);
			TreeColumn col = colv.getColumn();
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
				this.viewer.getTree().addListener(SWT.PaintItem, (Listener) man);
			}
			;
			colv.setEditingSupport(man.getEditingSupport(this.viewer));
		}
		ColumnViewerToolTipSupport.enableFor(this.viewer, ToolTip.NO_RECREATE);
		this.viewer.getTree().setHeaderVisible(true);
	}

	@Override
	public void updateColumns(String newValue) {
		TreeColumn[] acolumns = this.viewer.getTree().getColumns();
		int order[] = new int[acolumns.length];
		String[] prefValues = newValue.split(",");
		if (prefValues.length == 0)
			return;
		HashMap<String, Integer> colOrger = new HashMap();
		HashSet<Integer> orderGaps = new HashSet();
		for (int i = 0; i < prefValues.length; i++) {
			Integer integer = Integer.valueOf(i);
			colOrger.put(prefValues[i], integer);
			orderGaps.add(integer);
		}
		for (int i = 0; i < acolumns.length; i++) {
			TreeColumn acol = acolumns[i];
			AbstractColumn mcol = (AbstractColumn) columns.get(i);
			boolean checked = true;
			String key = mcol.getColumnFullName();
			Integer pos = colOrger.get(key);
			if (pos == null) {
				pos = colOrger.get("-" + key);
				if (pos != null)
					checked = false;
			}
			if (pos != null) {
				order[i] = pos.intValue();
				Integer ipos = Integer.valueOf(pos.intValue());
				if (orderGaps.contains(ipos))
					orderGaps.remove(ipos);
				else
					order[i] = -1; // duplicate keys!!
			} else {
				order[i] = -1;
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
			if (pos < 0) {
				Integer next = orderGaps.iterator().next();
				orderGaps.remove(next);
				order[i] = next.intValue();
			}
		}
		this.viewer.getTree().setColumnOrder(order);
	}
}
