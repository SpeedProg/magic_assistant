/**
 * 
 */
package com.reflexit.magiccards.ui.views;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;

import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class TableViewerContentProvider<T> implements IStructuredContentProvider {
	private Object input;

	@Override
	public void dispose() {
		// ignore
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// ignore
		this.input = newInput;
	}

	public int[] getIndices(IStructuredSelection selection) {
		if (selection.isEmpty())
			return new int[] {};
		ArrayList<Integer> res = new ArrayList<>();
		Object[] elements = getElements(input);
		for (Object element : selection.toArray()) {
			int i = 0;
			for (Object object : elements) {
				if (element.equals(object)) {
					res.add(i);
				}
				i++;
			}
		}
		int[] ind = new int[res.size()];
		for (int i = 0; i < ind.length; i++) {
			ind[i] = res.get(i);
		}
		return ind;
	}

	@Override
	public Object[] getElements(Object element) {
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

	public Object getParent(@SuppressWarnings("unused") Object element) {
		return null;
	}
}