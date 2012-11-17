/**
 * 
 */
package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICardGroup;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class LazyTreeViewContentProvider implements // IStructuredContentProvider,
		ILazyTreeContentProvider {
	LazyTreeViewContentProvider() {
	}

	private TreeViewer treeViewer;
	private IFilteredCardStore root;
	private ICardGroup rootGroup;

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (viewer instanceof TreeViewer) {
			this.treeViewer = (TreeViewer) viewer;
		}
		if (newInput instanceof IFilteredCardStore) {
			this.root = (IFilteredCardStore) newInput;
			this.rootGroup = root.getCardGroupRoot();
		} else
			this.root = null;
	}

	public Object getParent(Object child) {
		return null;
	}

	public void updateChildCount(Object element, int currentChildCount) {
		if (root == null)
			return;
		synchronized (root) {
			int count = 0;
			if (element instanceof IFilteredCardStore) {
				count = rootGroup.size();
			} else if (element instanceof ICardGroup) {
				count = ((ICardGroup) element).size();
			}
			if (count == 0)
				treeViewer.setHasChildren(element, false);
			else
				this.treeViewer.setChildCount(element, count);
		}
	}

	public void updateElement(Object parent, int index) {
		if (root == null)
			return;
		synchronized (root) {
			ICardGroup group = null;
			if (parent instanceof IFilteredCardStore) {
				group = rootGroup;
			} else if (parent instanceof ICardGroup) {
				group = (CardGroup) parent;
			} else
				return;
			Object child = group.getChildAtIndex(index);
			this.treeViewer.replace(parent, index, child);
			// System.err.println("grpup: " + " index " + index);
			if (child instanceof ICardGroup)
				updateChildCount(child, -1);
			else
				updateChildCount(child, 0);
		}
	}

	public int getSize(Object input) {
		if (!(input instanceof IFilteredCardStore)) {
			return 0;
		}
		synchronized (root) {
			int size = rootGroup.size();
			return size;
		}
	}

	public void dispose() {
		root = null;
		rootGroup = null;
	}
}