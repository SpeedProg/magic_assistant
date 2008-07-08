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

import com.reflexit.magiccards.ui.views.columns.ColumnManager;

public class LazyTreeViewerManager extends ViewerManager implements IDisposable {
	private TreeViewer viewer;

	LazyTreeViewerManager(AbstractCardsView view) {
		super(view.doGetFilteredStore(), view.getViewSite().getId());
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
		loadData();
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
		this.filter.setSortIndex(index);
		this.filter.setAscending(sortDirection == SWT.UP);
	}

	@Override
	protected void updateViewer() {
		long time = System.currentTimeMillis();
		//	if (this.viewer.getInput() != this.getDataHandler()) {
		this.viewer.setInput(this.getFilteredStore());
		System.err.println("set input1 tree time: " + (System.currentTimeMillis() - time) + " ms");
		int size = this.getFilteredStore().getSize();
		System.err.println("size=" + size);
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
		}
		ColumnViewerToolTipSupport.enableFor(this.viewer, ToolTip.NO_RECREATE);
		this.viewer.getTree().setHeaderVisible(true);
	}
}
