/**
 *
 */
package com.reflexit.magiccards.ui.gallery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class FlatTreeContentProvider implements ITreeContentProvider {
	private static final Object[] EMPTY_CHILDREN = new Object[] {};
	private IFilteredCardStore fstore;
	private boolean groupped;
	private int level = 1;
	private HashMap<Object, Object[]> cache = new HashMap<>();
	private ICardGroup top;

	@Override
	public void dispose() {
	}

	public void setLevel(int level) {
		if (this.level != level) {
			this.level = level;
			cache.clear();
		}
	}

	public int getLevel() {
		return level;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof IFilteredCardStore) {
			this.fstore = (IFilteredCardStore) newInput;
			groupped = this.fstore.getFilter().isGroupped();
			cache.clear();
			top = new CardGroup(null, "Groups");
		}
	}

	@Override
	public Object[] getChildren(Object element) {
		if (cache.containsKey(element))
			return cache.get(element);
		Object[] res = null;
		if (element.equals(top) && top instanceof CardGroup) {
			res = top.getChildren();
		} else if (element instanceof CardGroup) {
			Collection children = ((CardGroup) element).expand();
			res = children.toArray(new Object[children.size()]);
		} else if (element instanceof IFilteredCardStore) {
			ICardGroup root = fstore.getCardGroupRoot();
			if (groupped) {
				if (level > 1) {
					Collection<ICardGroup> children = leafGroups(root);
					if (children.size() > 200) {
						CardGroup tg = (CardGroup) top;
						for (ICardGroup group : children) {
							CardGroup ng = new CardGroup(group.getFieldIndex(), getRecName(group));
							tg.add(ng);
							ng.addAll(group.getChildrenList());
						}
						res = new Object[] { top };
						cache.put(top, top.getChildren());
					} else
						res = children.toArray(new Object[children.size()]);
				}
				if (res == null) {
					res = root.getChildren();
				}
			} else {
				res = new Object[] { root };
			}
		}
		if (res == null)
			res = EMPTY_CHILDREN;
		cache.put(element, res);
		return res;
	}

	private String getRecName(ICardGroup group) {
		ICardGroup parent = group.getParent();
		if (parent == null)
			return group.getName();
		String parentText = getRecName(parent);
		if (parentText.isEmpty() || parentText.equals("All") || parent.depth() == 1)
			return group.getName();
		else
			return parentText + "/" + group.getName();
	}

	private Collection<ICardGroup> leafGroups(ICardGroup cardGroup) {
		return leafGroups((CardGroup) cardGroup, new ArrayList<>());
	}

	private Collection<ICardGroup> leafGroups(CardGroup cardGroup, List<ICardGroup> result) {
		Collection<CardGroup> subs = cardGroup.getSubGroups();
		if (subs.size() == 0 || level == cardGroup.depth()) {
			// this is leaf node
			result.add(cardGroup);
			return result;
		}
		for (ICard card : cardGroup.getChildren()) {
			if (card instanceof CardGroup) {
				leafGroups((CardGroup) card, result);
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