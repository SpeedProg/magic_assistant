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
import com.reflexit.magiccards.core.model.IMagicCard;
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
		} else if (element instanceof List) {
			// groups list
			List list = (List) element;
			if (list.size() == 0)
				return null;
			Object last = list.get(list.size() - 1);
			if (last instanceof CardGroup) {
				return getChildren(last);
			}
		} else if (element instanceof IFilteredCardStore) {
			if (groupped) {
				if (level > 1) {
					Collection children = leafs(fstore.getCardGroupRoot());
					return children.toArray(new Object[children.size()]);
				}
				return fstore.getCardGroupRoot().getChildren();
			} else
				return new Object[] { fstore.getCardGroupRoot() };
		}
		return null;
	}

	private Collection<IMagicCard> leafs(ICardGroup cardGroup) {
		return leafs((CardGroup) cardGroup, new ArrayList<>(), new ArrayList<>());
	}

	private Collection<IMagicCard> leafs(CardGroup cardGroup, List glist, List result) {
		Collection<CardGroup> subs = cardGroup.getSubGroups();
		if (subs.size() == 0 || level == glist.size()) {
			// this is leaf node
			List newGList = new ArrayList<>(glist);
			result.add(newGList);
			return result;
		}
		for (Iterator<? extends ICard> iterator = subs.iterator(); iterator.hasNext();) {
			ICard card = iterator.next();
			if (card instanceof CardGroup) {
				List newGList = new ArrayList<>(glist);
				newGList.add(card);
				leafs((CardGroup) card, newGList, result);
			}
		}
		return result;
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