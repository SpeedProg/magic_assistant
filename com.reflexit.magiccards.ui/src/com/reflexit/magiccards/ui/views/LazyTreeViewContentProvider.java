/**
 * 
 */
package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class LazyTreeViewContentProvider implements // IStructuredContentProvider,
		ILazyTreeContentProvider {
	LazyTreeViewContentProvider() {
	}

	private TreeViewer treeViewer;
	private IFilteredCardStore root;
	private ICardGroup rootGroup;
	private boolean showRoot = true;

	@Override
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

	@Override
	public Object getParent(Object child) {
		return null;
	}

	@Override
	public void updateChildCount(Object element, int currentChildCount) {
		if (root == null)
			return;
		synchronized (root) {
			int count = 0;
			if (element instanceof IFilteredCardStore) {
				if (showRoot)
					count = 1;
				else
					count = rootGroup.size();
			} else if (element instanceof ICardGroup) {
				count = ((ICardGroup) element).size();
			} else if (element instanceof MagicCard) {
				// count = ((MagicCard) element).getPhysicalCards().size();
			}
			if (count == 0)
				treeViewer.setChildCount(element, 0);
			else
				treeViewer.setChildCount(element, count);
		}
	}

	@Override
	public void updateElement(Object parent, int index) {
		if (root == null)
			return;
		synchronized (root) {
			ICardGroup group = null;
			if (parent instanceof IFilteredCardStore) {
				group = rootGroup;
				if (showRoot) {
					this.treeViewer.replace(parent, index, group);
					updateChildCount(group, group.size());
					return;
				}
			} else if (parent instanceof ICardGroup) {
				group = (CardGroup) parent;
			} else if (parent instanceof MagicCard) {
				// group = ((MagicCard) parent).getPhysicalCardsGroup();
			}
			if (group == null)
				return;
			Object child = group.getChildAtIndex(index);
			this.treeViewer.replace(parent, index, child);
			updateChildCount(child, -1);
		}
	}

	public int getSize(Object input) {
		if (!(input instanceof IFilteredCardStore)) {
			return 0;
		}
		if (showRoot)
			return 1;
		synchronized (root) {
			int size = rootGroup.size();
			return size;
		}
	}

	@Override
	public void dispose() {
		root = null;
		rootGroup = null;
	}
}