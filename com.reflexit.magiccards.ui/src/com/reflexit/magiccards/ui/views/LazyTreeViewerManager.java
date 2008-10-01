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
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.services.IDisposable;

import java.util.HashMap;

import com.reflexit.magiccards.core.model.IFilteredCardStore;
import com.reflexit.magiccards.ui.views.columns.ColumnManager;

public class LazyTreeViewerManager extends ViewerManager implements IDisposable {
	private MyTreeViewer viewer;

	LazyTreeViewerManager(AbstractCardsView view) {
		super(view.doGetFilteredStore(), view.getPreferenceStore(), view.getViewSite().getId());
	}

	/**
	 * @param filteredStore
	 * @param view
	 */
	public LazyTreeViewerManager(IFilteredCardStore filteredStore, AbstractCardsView view) {
		super(filteredStore, view.getPreferenceStore(), view.getViewSite().getId());
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
		this.viewer = new MyTreeViewer(parent, SWT.FULL_SELECTION | SWT.VIRTUAL);
		// drillDownAdapter = new DrillDownAdapter(viewer);
		// this.viewer.setContentProvider(new RegularViewContentProvider());
		this.viewer.setContentProvider(new LazyTreeViewContentProvider());
		//	this.viewer.setLabelProvider(new MagicCardLabelProvider());
		this.viewer.setUseHashlookup(true);
		this.viewer.getTree().setDragDetect(true);
		// viewer.setSorter(new NameSorter());
		createDefaultColumns();
		return this.viewer.getControl();
	}

	@Override
	protected void updateSortColumn(int index) {
		this.viewer.getTree().setSortColumn(this.viewer.getTree().getColumn(index));
		int sortDirection = this.viewer.getTree().getSortDirection();
		if (sortDirection != SWT.DOWN)
			sortDirection = SWT.DOWN;
		else
			sortDirection = SWT.UP;
		this.viewer.getTree().setSortDirection(sortDirection);
		ColumnManager man = (ColumnManager) this.viewer.getLabelProvider(index);
		this.filter.setSortIndex(man.getSortIndex());
		this.filter.setAscending(sortDirection == SWT.UP);
	}

	@Override
	protected void updateViewer() {
		long time = System.currentTimeMillis();
		//	if (this.viewer.getInput() != this.getDataHandler()) {
		this.viewer.unmapAllElements();
		this.viewer.setInput(this.getFilteredStore());
		System.err.println("set input1 tree time: " + (System.currentTimeMillis() - time) + " ms");
		int size = this.getFilteredStore().getCardGroups().length;
		//System.err.println("size=" + size);
		this.viewer.getTree().setItemCount(size);
		//		} else {
		//			this.viewer.setSelection(new StructuredSelection());
		//			this.viewer.getTree().clearAll(true);
		//			((MyTreeViewer) this.viewer).unmapAllElements();
		//			this.viewer.refresh(true);
		//		}
		System.err.println("set input2 tree time: " + (System.currentTimeMillis() - time) + " ms");
	}

	protected void createDefaultColumns() {
		createColumnLabelProviders();
		for (int i = 0; i < getColumnsNumber(); i++) {
			ColumnManager man = (ColumnManager) this.columns.get(i);
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
		String[] indexes = newValue.split(",");
		if (indexes.length == 0)
			return;
		HashMap used = new HashMap();
		for (int i = 0; i < acolumns.length; i++) {
			TreeColumn col = acolumns[i];
			used.put(col, new Integer(i));
		}
		for (int i = 0; i < acolumns.length; i++) {
			try {
				String in = indexes[i];
				int xcol = Integer.parseInt(in);
				int col = xcol > 0 ? xcol - 1 : -xcol - 1;
				TreeColumn acol = acolumns[col];
				order[i] = col;
				boolean checked = xcol > 0;
				if (checked) {
					if (acol.getWidth() <= 0)
						acol.setWidth(((ColumnManager) this.columns.get(i)).getColumnWidth());
				} else {
					acol.setWidth(0);
				}
				used.remove(acol);
			} catch (RuntimeException e) {
				TableColumn acol = (TableColumn) used.keySet().iterator().next();
				order[i] = ((Integer) used.get(acol)).intValue();
				used.remove(acol);
			}
		}
		this.viewer.getTree().setColumnOrder(order);
	}
}
