/**
 *
 */
package com.reflexit.magiccards.ui.views.model;

import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class LazyTreeViewContentProvider implements // IStructuredContentProvider,
		ILazyTreeContentProvider {
	private TreeViewer treeViewer;
	private IFilteredCardStore root;
	private boolean showRoot = true;
	private int maxCount = 1000;

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (viewer instanceof TreeViewer) {
			this.treeViewer = (TreeViewer) viewer;
		}
		if (newInput instanceof IFilteredCardStore) {
			this.root = (IFilteredCardStore) newInput;
		} else
			this.root = null;
	}

	@Override
	public Object getParent(Object child) {
		return null;
	}

	@Override
	public void updateChildCount(Object element, int currentChildCount) {
		if (root == null)
			return;
		synchronized (root) {
			int count = getChildCount(element);
			treeViewer.setChildCount(element, count);
		}
	}

	private int getChildRealCount(Object element) {
		synchronized (root) {
			int count = 0;
			if (element instanceof IFilteredCardStore) {
				if (showRoot)
					count = 1;
				else
					count = root.getCardGroupRoot().size();
			} else if (element instanceof ICardGroup) {
				count = ((ICardGroup) element).size();
			}
			return count;
		}
	}

	private int getChildCount(Object element) {
		int count = getChildRealCount(element);
		if (count > maxCount)
			count = maxCount;
		return count;
	}

	@Override
	public void updateElement(Object parent, int index) {
		if (root == null)
			return;
		if (index >= maxCount)
			return;
		synchronized (root) {
			Object child = null;
			if (index == maxCount - 1) {
				child = createOverflow(getChildRealCount(parent));
				treeViewer.replace(parent, index, child);
				treeViewer.setChildCount(child, -1);
				return;
			}
			if (parent instanceof IFilteredCardStore) {
				if (showRoot) {
					child = root.getCardGroupRoot();
				} else {
					child = root.getCardGroupRoot().getChildAtIndex(index);
				}
			} else if (parent instanceof ICardGroup) {
				child = ((CardGroup) parent).getChildAtIndex(index);
			}
			if (child == null)
				return;
			treeViewer.replace(parent, index, child);
			treeViewer.setChildCount(child, getChildCount(child));
		}
	}

	protected Object createOverflow(int len) {
		return new CardGroup(null, "Too many tree items " + len + " max is " + getMaxCount());
	}

	public int getSize(Object input) {
		return getChildCount(input);
	}

	public boolean isShowRoot() {
		return showRoot;
	}

	public void setShowRoot(boolean showRoot) {
		this.showRoot = showRoot;
	}

	public int getMaxCount() {
		return maxCount;
	}

	public void setMaxCount(int maxCount) {
		this.maxCount = maxCount;
	}
}