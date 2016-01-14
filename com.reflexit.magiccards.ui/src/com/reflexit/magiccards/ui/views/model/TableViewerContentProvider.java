/**
 * 
 */
package com.reflexit.magiccards.ui.views.model;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;

import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class TableViewerContentProvider<T> implements IStructuredContentProvider, ISelectionTranslator {
	private Object input;

	@Override
	public void dispose() {
		input = null;
	}

	public Object getInput() {
		return input;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.input = newInput;
	}

	public int[] getIndices(IStructuredSelection selection) {
		if (selection.isEmpty())
			return new int[] {};
		Object[] elements = getElements(input);
		ArrayList<Integer> res = new ArrayList<>();
		for (Object element : selection.toArray()) {
			int i = indexOf(element, elements);
			res.add(i);
		}
		int[] ind = new int[res.size()];
		for (int i = 0; i < ind.length; i++) {
			ind[i] = res.get(i);
		}
		return ind;
	}

	@Override
	public IStructuredSelection translateSelection(IStructuredSelection selection, int level) {
		if (selection.isEmpty())
			return selection;
		ArrayList<Object> res = new ArrayList<>();
		Object[] elements = getElements(input);
		for (Object object : selection.toArray()) {
			if (object instanceof TreePath) {
				object = ((TreePath) object).getLastSegment();
			}
			int i = indexOf(object, elements);
			if (i >= 0)
				res.add(elements[i]);
			else
				res.add(object);
		}
		return new StructuredSelection(res);
	}

	public int indexOf(Object element, Object[] elements) {
		int i = 0;
		for (Object object : elements) {
			if (element.equals(object)) {
				return i;
			}
			i++;
		}
		i = 0;
		for (Object object : elements) {
			if (object instanceof ICardGroup) {
				int j = indexOf(element, ((ICardGroup) object).getChildren());
				if (j >= 0)
					return i;
			}
			i++;
		}
		return -1;
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
			return fstore.getCardGroupRoot().getChildren();
		}
		return new Object[0];
	}

	protected boolean isFlat(IFilteredCardStore<T> fstore) {
		return fstore.getFilter().getGroupField() == null;
	}

	public Object getParent(@SuppressWarnings("unused") Object element) {
		return null;
	}
}