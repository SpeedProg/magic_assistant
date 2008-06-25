/**
 * 
 */
package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.reflexit.magiccards.core.model.IFilteredCardStore;
import com.reflexit.magiccards.core.model.IMagicCard;

class LazyTreeViewContentProvider implements // IStructuredContentProvider,
        ILazyTreeContentProvider {
	/**
	 * 
	 */
	/**
	 * @param view
	 */
	LazyTreeViewContentProvider() {
	}
	// private MagicCardResultHandler resultSet;
	// private DeferredTreeContentManager manager;
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

	public void dispose() {
	}

	public Object getParent(Object child) {
		return null;
	}

	public void updateChildCount(Object element, int currentChildCount) {
		if (element instanceof IMagicCard) {
			this.treeViewer.setChildCount(element, 0);
		} else if (element instanceof IFilteredCardStore) {
			if (currentChildCount <= 0) {
				int count = ((IFilteredCardStore) element).getSize();
				this.treeViewer.setChildCount(element, count);
			}
		}
	}
	// private void init(Object element) {
	// if (MagicCardResultHandler == null && element instanceof
	// MagicCardResultHandler) {
	// try {
	// MagicCardResultHandler = (MagicCardResultHandler) element;
	// MagicCardResultHandlerMetaData metaData =
	// MagicCardResultHandler.getMetaData();
	// columns = metaData.getColumnCount();
	// } catch (SQLException e) {
	// Activator.log(e);
	// }
	// }
	// }
	private int level = 0;

	public void updateElement(Object parent, int index) {
		if (parent instanceof IFilteredCardStore) {
			IFilteredCardStore store = (IFilteredCardStore) parent;
			int i = index;
			{
				System.err.println("level: " + this.level + " index " + index);
				// int l = Math.min(index + 20,
				// magicCardResultHandler.getSize());
				// for (int i = index; i < l; i++) {
				try {
					Object element = store.getElement(index);
					// level++;
					this.treeViewer.replace(parent, i, element);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			// level--;
			// updateChildCount(element, -1);
			// view.getViewer().setChildCount(row, 0);
		}
	}
}