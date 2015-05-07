/**
 *
 */
package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

class LazyTableViewContentProvider implements ILazyContentProvider {
	LazyTableViewContentProvider() {
	}

	private TableViewer tableViewer;
	private IFilteredCardStore root;

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (viewer instanceof TableViewer) {
			this.tableViewer = (TableViewer) viewer;
		}
		if (newInput instanceof IFilteredCardStore)
			this.root = (IFilteredCardStore) newInput;
		else
			this.root = null;
	}

	@Override
	public void dispose() {
		this.tableViewer = null;
		this.root = null;
	}

	public int getSize(Object newInput) {
		if (newInput instanceof IFilteredCardStore) {
			return ((IFilteredCardStore) newInput).getSize();
		}
		return 0;
	}

	@Override
	public void updateElement(int index) {
		if (this.root != null) {
			if (index >= root.getSize()) {
				// element is gone...
			} else {
				Object element = this.root.getElement(index);
				MagicLogger.trace("table update element " + index + " " + element);
				this.tableViewer.replace(element, index);
			}
		}
	}
}