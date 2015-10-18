package com.reflexit.magiccards.ui.gallery;

import java.util.ArrayList;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class GroupContentProvider implements ITreeContentProvider {
	private static final Object[] EMPTY_CHILDREN = new Object[] {};
	private IFilteredCardStore fstore;
	private boolean groupped;

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof IFilteredCardStore) {
			this.fstore = (IFilteredCardStore) newInput;
			groupped = this.fstore.getFilter().isGroupped();
		}
	}

	@Override
	public Object[] getChildren(Object element) {
		Object[] res = null;
		if (element instanceof CardGroup) {
			ArrayList ares = new ArrayList<>();
			ICard[] children = ((CardGroup) element).getChildren();
			for (ICard card : children) {
				if (card instanceof ICardGroup) {
					// && ((ICardGroup) card).getFieldIndex() != MagicCardField.NAME
					ares.add(card);
				}
				if (ares.size() > 1000) {
					ares.add(new CardGroup(null, "Too many groups " + children.length));
					break;
				}
			}
			res = ares.toArray(new Object[ares.size()]);
		} else if (element instanceof IFilteredCardStore) {
			ICardGroup root = fstore.getCardGroupRoot();
			res = new Object[] { root };
		}
		if (res == null)
			res = EMPTY_CHILDREN;
		return res;
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof ICardGroup) {
			return ((ICardGroup) element).size() > 0;
		} else if (element instanceof IFilteredCardStore) {
			if (!groupped)
				return false;
			IFilteredCardStore fstore = (IFilteredCardStore) element;
			return fstore.getCardGroupRoot().size() > 0;
		}
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}
}
