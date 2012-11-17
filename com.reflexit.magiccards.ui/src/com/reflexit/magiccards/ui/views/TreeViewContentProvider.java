/**
 * 
 */
package com.reflexit.magiccards.ui.views;

import java.util.Collection;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.reflexit.magiccards.core.model.ICardGroup;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class TreeViewContentProvider<T> implements ITreeContentProvider {
	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public Object[] getChildren(Object element) {
		if (element instanceof ICardGroup) {
			return ((ICardGroup) element).getChildren();
		} else if (element instanceof Collection) {
			Collection children = (Collection) element;
			return children.toArray(new Object[children.size()]);
		} else if (element instanceof IFilteredCardStore) {
			IFilteredCardStore<T> fstore = (IFilteredCardStore<T>) element;
			if (isFlat(fstore)) {
				return fstore.getElements();
			} else
				return fstore.getCardGroupRoot().getChildren();
		}
		return null;
	}

	private boolean isFlat(IFilteredCardStore<T> fstore) {
		return fstore.getFilter().getGroupField() == null;
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof ICardGroup) {
			return ((ICardGroup) element).size() > 0;
		} else if (element instanceof Collection) {
			Collection children = (Collection) element;
			return children.size() > 0;
		} else if (element instanceof IFilteredCardStore) {
			IFilteredCardStore<T> fstore = (IFilteredCardStore<T>) element;
			if (isFlat(fstore)) {
				return fstore.getSize() > 0;
			} else
				return fstore.getCardGroupRoot().size() > 0;
		}
		return false;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}
}