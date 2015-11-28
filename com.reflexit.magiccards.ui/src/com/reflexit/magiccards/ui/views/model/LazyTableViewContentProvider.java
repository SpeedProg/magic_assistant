/**
 *
 */
package com.reflexit.magiccards.ui.views.model;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class LazyTableViewContentProvider implements ILazyContentProvider, ISizeContentProvider {
	private boolean inChange = false;
	private TableViewer tableViewer;
	private IFilteredCardStore root;

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (viewer instanceof TableViewer) {
			this.tableViewer = (TableViewer) viewer;
		}
		if (newInput instanceof IFilteredCardStore) {
			this.root = (IFilteredCardStore) newInput;
			tableViewer.setItemCount(root.getSize());
		} else
			this.root = null;
	}

	@Override
	public void dispose() {
		this.tableViewer = null;
		this.root = null;
	}

	@Override
	public int getSize(Object newInput) {
		if (newInput instanceof IFilteredCardStore) {
			return ((IFilteredCardStore) newInput).getSize();
		}
		return 0;
	}

	public int[] getIndices(IStructuredSelection selection) {
		if (root == null || selection.isEmpty())
			return new int[] {};
		ArrayList<Integer> res = new ArrayList<>();
		Object[] elements = this.root.getElements();
		for (Object element : selection.toArray()) {
			int i = 0;
			for (Object object : elements) {
				if (element.equals(object)) {
					res.add(i);
				}
				i++;
			}
		}
		int[] ind = new int[res.size()];
		for (int i = 0; i < ind.length; i++) {
			ind[i] = res.get(i);
		}
		return ind;
	}

	@Override
	public void updateElement(int index) {
		synchronized (this) {
			if (inChange)
				return;
			inChange = true;
		}
		try {
			if (this.root != null) {
				if (index >= root.getSize()) {
					// element is gone...
					tableViewer.setItemCount(root.getSize());
				} else {
					Object element = this.root.getElement(index);
					// MagicLogger.trace("table update element " + index + " " +
					// element);
					if (element == null) {
						tableViewer.setItemCount(root.getSize());
						return;
					}
					this.tableViewer.replace(element, index);
				}
			}
		} finally {
			synchronized (this) {
				inChange = false;
			}
		}
	}
}