/**
 * 
 */
package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class LazyTreeViewContentProvider implements // IStructuredContentProvider,
		ILazyTreeContentProvider {
	LazyTreeViewContentProvider() {
	}

	private TreeViewer treeViewer;
	private IFilteredCardStore root;

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (viewer instanceof TreeViewer) {
			this.treeViewer = (TreeViewer) viewer;
		}
		if (newInput instanceof IFilteredCardStore)
			this.root = (IFilteredCardStore) newInput;
		else
			this.root = null;
	}

	public Object getParent(Object child) {
		return null;
	}

	public void updateChildCount(Object element, int currentChildCount) {
		synchronized (root) {
			int count = 0;
			if (element instanceof IFilteredCardStore) {
				count = ((IFilteredCardStore) element).getCardGroups().length;
			} else if (element instanceof CardGroup) {
				count = ((CardGroup) element).size();
			}
			if (count == 0)
				treeViewer.setHasChildren(element, false);
			else
				this.treeViewer.setChildCount(element, count);
		}
	}

	public void updateElement(Object parent, int index) {
		synchronized (root) {
			if (parent instanceof IFilteredCardStore) {
				IFilteredCardStore store = (IFilteredCardStore) parent;
				// System.err.println("store: " + " index " + index);
				try {
					Object element = store.getCardGroup(index);
					this.treeViewer.replace(parent, index, element);
					updateChildCount(element, -1);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			} else if (parent instanceof CardGroup) {
				CardGroup group = (CardGroup) parent;
				Object child = group.getChildAtIndex(index);
				this.treeViewer.replace(parent, index, child);
				// System.err.println("grpup: " + " index " + index);
				if (child instanceof CardGroup)
					updateChildCount(child, -1);
				else
					updateChildCount(child, 0);
			}
		}
	}

	public int getSize(Object input) {
		if (!(input instanceof IFilteredCardStore)) {
			return 0;
		}
		synchronized (root) {
			int size = ((IFilteredCardStore) input).getCardGroups().length;
			return size;
		}
	}

	public void dispose() {
		root = null;
	}
}