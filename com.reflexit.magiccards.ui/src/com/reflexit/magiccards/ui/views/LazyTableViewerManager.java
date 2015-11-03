package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class LazyTableViewerManager extends TableViewerManager {
	static class MyTableViewer extends TableViewer {
		public MyTableViewer(Composite parent, int style) {
			super(parent, style);
		}

		@Override
		public void unmapAllElements() {
			super.unmapAllElements();
		}

		@Override
		public void setSelection(ISelection selection, boolean reveal) {
			if (selection instanceof IStructuredSelection) {
				LazyTableViewContentProvider provider = (LazyTableViewContentProvider) getContentProvider();
				int[] indices = provider.getIndices((IStructuredSelection) selection);
				getTable().setSelection(indices);
				getTable().showSelection();
			}
		}
	}

	public LazyTableViewerManager(String id) {
		super(id);
	}

	@Override
	public Control createContents(Composite parent) {
		this.viewer = new MyTableViewer(parent,
				SWT.FULL_SELECTION | SWT.MULTI | SWT.VIRTUAL | SWT.BORDER | SWT.H_SCROLL);
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
		int sortDirection = getSortDirection();
		if (index >= 0) {
			if (direction == -1)
				sortDirection = SWT.DOWN;
			else if (direction == 0)
				sortDirection = SWT.NONE;
			else
				sortDirection = SWT.UP;
		}
		setControlSortColumn(index, sortDirection);
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
