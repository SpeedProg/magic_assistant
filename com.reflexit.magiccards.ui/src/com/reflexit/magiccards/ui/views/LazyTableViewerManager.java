package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableColumn;

public class LazyTableViewerManager extends TableViewerManager {
	static class MyTableViewer extends TableViewer {
		public MyTableViewer(Composite parent, int style) {
			super(parent, style);
		}

		@Override
		public void unmapAllElements() {
			super.unmapAllElements();
		}
	}

	public LazyTableViewerManager(String id) {
		super(id);
	}

	@Override
	public Control createContents(Composite parent) {
		this.viewer = new MyTableViewer(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.VIRTUAL);
		this.viewer.getTable().setFont(getFont());
		// drillDownAdapter = new DrillDownAdapter(viewer);
		// this.viewer.setContentProvider(new RegularViewContentProvider());
		this.viewer.setContentProvider(new LazyTableViewContentProvider());
		this.viewer.setUseHashlookup(true);
		updateGrid();
		// viewer.setSorter(new NameSorter());
		createDefaultColumns();
		return this.viewer.getControl();
	}

	@Override
	protected LazyTableViewContentProvider getContentProvider() {
		return (LazyTableViewContentProvider) getViewer().getContentProvider();
	}

	@Override
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
	public void updateViewer(Object filteredStore) {
		if (this.viewer.getControl().isDisposed())
			return;
		updateTableHeader();
		updateGrid();
		// long time = System.currentTimeMillis();
		int size = getContentProvider().getSize(filteredStore);
		if (this.viewer.getInput() != filteredStore) {
			this.viewer.setInput(filteredStore);
			this.viewer.setItemCount(size);
		} else {
			this.viewer.setSelection(new StructuredSelection());
			this.viewer.getTable().clearAll();
			((MyTableViewer) this.viewer).unmapAllElements();
			this.viewer.setItemCount(size);
			this.viewer.refresh(true);
		}
		// System.err.println("set input time: " + (System.currentTimeMillis() -
		// time) + " ms");
	}
}
