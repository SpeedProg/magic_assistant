/**
 * 
 */
package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

class LazyTableViewContentProvider implements ILazyContentProvider {
	LazyTableViewContentProvider() {
	}
	private TableViewer tableViewer;
	private IFilteredCardStore root;

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (viewer instanceof TableViewer) {
			this.tableViewer = (TableViewer) viewer;
		}
		if (newInput instanceof IFilteredCardStore)
			this.root = (IFilteredCardStore) newInput;
		else
			this.root = null;
	}

	public void dispose() {
		this.tableViewer = null;
		this.root = null;
	}

	public void updateElement(int index) {
		if (this.root instanceof IFilteredCardStore) {
			Object element = this.root.getElement(index);
			this.tableViewer.replace(element, index);
		}
	}
}