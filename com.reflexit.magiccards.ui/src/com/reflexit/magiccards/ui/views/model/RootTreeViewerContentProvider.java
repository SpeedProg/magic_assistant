package com.reflexit.magiccards.ui.views.model;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class RootTreeViewerContentProvider implements ITreeContentProvider, ISelectionTranslator, ISizeContentProvider {
	private Object[] rootChildren;
	private Object root;
	private TreeViewerContentProvider sub;
	private Object input;

	public RootTreeViewerContentProvider() {
		sub = new TreeViewerContentProvider();
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (!(newInput == null || newInput instanceof IFilteredCardStore || newInput instanceof Iterable))
			throw new IllegalArgumentException("Unknown type of input for RootTree content provider: " + newInput);
		sub.inputChanged(viewer, oldInput, newInput);
		rootChildren = sub.getChildren(newInput);
		root = getRoot(newInput);
		input = newInput;
	}

	public Object getInput() {
		return input;
	}

	private Object getRoot(Object element) {
		if (element instanceof IFilteredCardStore) {
			ICardGroup root = ((IFilteredCardStore) element).getCardGroupRoot();
			return root;
		}
		if (element instanceof Iterable) {
			CardGroup root = new CardGroup(null, "All");
			root.addAll((Iterable) element);
			return root;
		}
		return null;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object element) {
		if (element == getInput()) {
			if (rootChildren.length == 0)
				return TreeViewerContentProvider.EMPTY_CHILDREN;
			if (root != null)
				return new Object[] { root };
		}
		if (element == root) {
			return rootChildren;
		}
		return sub.getChildren(element);
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
		return sub.getSize(element);
	}

	@Override
	public void dispose() {
		sub.dispose();
	}

	@Override
	public IStructuredSelection translateSelection(IStructuredSelection selection, int level) {
		return sub.translateSelection(selection, level);
	}

	@Override
	public Object getParent(Object element) {
		return sub.getParent(element);
	}

	@Override
	public boolean hasChildren(Object element) {
		return getSize(element) > 0;
	}

	public int getMaxCount() {
		return sub.getMaxCount();
	}

	public void setMaxCount(int maxCount) {
		sub.setMaxCount(maxCount);
	}
}
