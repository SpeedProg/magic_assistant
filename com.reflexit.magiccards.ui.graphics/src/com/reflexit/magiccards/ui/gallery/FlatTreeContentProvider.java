/**
 *
 */
package com.reflexit.magiccards.ui.gallery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class FlatTreeContentProvider implements ITreeContentProvider {
	private IFilteredCardStore fstore;
	private boolean groupped;
	private int level = 1;

	@Override
	public void dispose() {
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
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
		if (element instanceof CardGroup) {
			Collection children = ((CardGroup) element).expand();
			return children.toArray(new Object[children.size()]);
		} else if (element instanceof IFilteredCardStore) {
			if (groupped) {
				if (level > 1) {
					Collection<ICardGroup> children = leafs(fstore.getCardGroupRoot());
					return children.toArray(new Object[children.size()]);
				}
				return fstore.getCardGroupRoot().getChildren();
			} else
				return new Object[] { fstore.getCardGroupRoot() };
		}
		return null;
	}

	private Collection<ICardGroup> leafs(ICardGroup cardGroup) {
		return leafs((CardGroup) cardGroup, new ArrayList<>());
	}

	private Collection<ICardGroup> leafs(CardGroup cardGroup, List<ICardGroup> result) {
		Collection<CardGroup> subs = cardGroup.getSubGroups();
		if (subs.size() == 0 || level == depth(cardGroup)) {
			// this is leaf node
			result.add(cardGroup);
			return result;
		}
		for (Iterator<? extends ICard> iterator = subs.iterator(); iterator.hasNext();) {
			ICard card = iterator.next();
			if (card instanceof CardGroup) {
				leafs((CardGroup) card, result);
			}
		}
		return result;
	}

	private int depth(ICardGroup cardGroup) {
		if (cardGroup.getParent() == null)
			return 1;
		return depth(cardGroup.getParent()) + 1;
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