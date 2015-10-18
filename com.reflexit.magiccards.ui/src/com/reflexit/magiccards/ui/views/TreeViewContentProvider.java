/**
 *
 */
package com.reflexit.magiccards.ui.views;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class TreeViewContentProvider implements ITreeContentProvider {
	public static final Object[] EMPTY_CHILDREN = new Object[] {};
	private int maxCount = 200;

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	protected int getChildCount(Object element) {
		int count = 0;
		if (element instanceof IFilteredCardStore) {
			IFilteredCardStore fstore = (IFilteredCardStore) element;
			ICardGroup root = fstore.getCardGroupRoot();
			count = getChildCount(root);
		} else if (element instanceof ICardGroup) {
			count = ((ICardGroup) element).size();
		}
		if (count > getMaxCount())
			count = getMaxCount();
		return count;
	}

	@Override
	public Object[] getChildren(Object element) {
		Object[] res;
		if (element instanceof ICardGroup) {
			res = ((ICardGroup) element).getChildren();
		} else if (element instanceof Collection) {
			Collection children = (Collection) element;
			res = children.toArray(new Object[children.size()]);
		} else if (element instanceof IFilteredCardStore) {
			IFilteredCardStore fstore = (IFilteredCardStore) element;
			ICardGroup root = fstore.getCardGroupRoot();
			res = getChildren(root);
		} else {
			return EMPTY_CHILDREN;
		}
		int m = getMaxCount();
		if (res.length > m) {
			Object o = createOverflow(res);
			res = Arrays.copyOf(res, m);
			res[m - 1] = o;
		}
		return res;
	}

	protected Object createOverflow(Object[] res) {
		return new CardGroup(null, "Too many tree items " + res.length + " max is " + getMaxCount());
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof ICardGroup) {
			return ((ICardGroup) element).getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return getChildCount(element) > 0;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public int getMaxCount() {
		return maxCount;
	}

	public void setMaxCount(int maxCount) {
		this.maxCount = maxCount;
	}
}