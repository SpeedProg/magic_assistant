package com.reflexit.magiccards.ui.views.model;

import org.eclipse.jface.viewers.Viewer;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class RootTreeViewerContentProvider extends TreeViewerContentProvider {
	private Object[] rootChildren;
	private Object root;

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		super.inputChanged(viewer, oldInput, newInput);
		rootChildren = super.getChildren(newInput);
		root = getRoot(newInput);
	}

	private Object getRoot(Object element) {
		if (element instanceof IFilteredCardStore) {
			ICardGroup root = ((IFilteredCardStore) element).getCardGroupRoot();
			return root;
		}
		if (element instanceof ICardGroup) {
			return element;
		}
		if (element instanceof Iterable) {
			CardGroup root = new CardGroup(null, "All");
			root.addAll((Iterable) element);
			return root;
		}
		return element;
	}

	@Override
	public Object[] getChildren(Object element) {
		if (element == getInput()) {
			if (rootChildren.length == 0)
				return EMPTY_CHILDREN;
			return new Object[] { root };
		}
		if (element == root) {
			return rootChildren;
		}
		return super.getChildren(element);
	}

	@Override
	public int getSize(Object element) {
		if (element == getInput()) {
			if (rootChildren.length == 0)
				return 0;
			return 1;
		}
		if (element == root) {
			return rootChildren.length;
		}
		return super.getSize(element);
	}
}
