package com.reflexit.magiccards.ui.views;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class RegularViewContentProvider implements ITreeContentProvider {
	@Override
	public Object[] getChildren(Object parent) {
		if (parent instanceof IFilteredCardStore) {
			IFilteredCardStore resultSet = (IFilteredCardStore) parent;
			return resultSet.getElements();
		} else {
			return null;
		}
	}

	@Override
	public Object getParent(Object child) {
		if (child instanceof IFilteredCardStore) {
			return null;
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IFilteredCardStore) {
			return true;
		}
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

	private TreeViewer viewer;
	private IFilteredCardStore root;

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (viewer instanceof TreeViewer) {
			// manager = new DeferredTreeContentManager(this, (TreeViewer)
			// viewer);
			this.viewer = (TreeViewer) viewer;
			if (newInput instanceof IFilteredCardStore)
				this.root = (IFilteredCardStore) newInput;
			else
				this.root = null;
		}
	}
}
