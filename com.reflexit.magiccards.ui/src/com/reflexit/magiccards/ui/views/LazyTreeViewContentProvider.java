/**
 * 
 */
package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

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
			int count = ((IFilteredCardStore) element).getCardGroups().length;
			this.treeViewer.setChildCount(element, count);
		} else if (element instanceof CardGroup) {
			int count = ((CardGroup) element).getChildren().size();
			this.treeViewer.setChildCount(element, count);
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
			{
				// System.err.println("store: " + " index " + index);
				try {
					Object element = store.getCardGroup(index);
					this.treeViewer.replace(parent, index, element);
					updateChildCount(element, -1);
				} catch (Throwable e) {
					e.printStackTrace();
				}
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

	public int getSize(Object input) {
		if (!(input instanceof IFilteredCardStore)) {
			return 0;
		}
		int size = ((IFilteredCardStore) input).getCardGroups().length;
		return size;
	}
}