/**
 *
 */
package com.reflexit.magiccards.ui.gallery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.views.model.TreeViewerContentProvider;

public class GroupExpandContentProvider2 implements ITreeContentProvider {
	private Object input;
	private Collection<?> top;

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (this.input == newInput)
			return;
		this.input = newInput;
		this.top = getTopLevel(newInput);
		// viewer.refresh();
	}

	public Collection getTopLevel(Object element) {
		if (element instanceof CardGroup) {
			CardGroup group = (CardGroup) element;
			Collection<CardGroup> subGroups = group.getSubGroups();
			if (subGroups.size() == 0) {
				return Collections.singletonList(element);
			}
			if (subGroups.size() > 1) {
				CardGroup first = subGroups.iterator().next();
				if (first.getFieldIndex() == MagicCardField.NAME)
					return Collections.singletonList(element);
			}
			return group.getChildrenList();
		} else if (element instanceof IFilteredCardStore) {
			ICardGroup root = ((IFilteredCardStore<?>) element).getCardGroupRoot();
			return root.getChildrenList();
		} else if (element instanceof Collection) {
			Collection list = (Collection) element;
			if (list.size() == 1) {
				Object first = list.iterator().next();
				if (first instanceof CardGroup) {
					if (((CardGroup) first).getFieldIndex() == MagicCardField.NAME)
						return list;
				}
				return getTopLevel(first);
			}
			return list;
		} else if (element instanceof Object[]) {
			return Arrays.asList((Object[]) element);
		} else if (element instanceof ICard) {
			CardGroup cardGroup = new CardGroup(null, "All");
			cardGroup.add((ICard) element);
			return Collections.singletonList(cardGroup);
		}
		return new ArrayList<>();
	}

	@Override
	public Object[] getChildren(Object element) {
		Object[] res = null;
		if (element == input) {
			return top.toArray();
		}
		if (element == top) {
			return top.toArray();
		}
		if (element instanceof CardGroup) {
			CardGroup group = (CardGroup) element;
			Collection<ICard> children = daexpand(group);
			return children.toArray(new Object[children.size()]);
		}
		if (element instanceof IMagicCard) {
			return TreeViewerContentProvider.EMPTY_CHILDREN;
		}
		System.err.println("Unknonwn child " + element);
		return res;
	}

	private Collection<ICard> daexpand(ICardGroup cardGroup) {
		return daexpand((CardGroup) cardGroup, new ArrayList<>());
	}

	private Collection<ICard> daexpand(Collection<?> collection) {
		return daexpand(collection, new ArrayList<>());
	}

	private Collection<ICard> daexpand(Collection<?> collection, List<ICard> result) {
		for (Object object : collection) {
			if (object instanceof CardGroup) {
				daexpand((CardGroup) object, result);
			} else if (object instanceof ICard) {
				result.add((ICard) object);
			}
		}
		return result;
	}

	private Collection<ICard> daexpand(CardGroup cardGroup, List<ICard> result) {
		for (Object object : cardGroup.getChildrenList()) {
			if (object instanceof CardGroup && ((CardGroup) object).getFieldIndex() != MagicCardField.NAME) {
				daexpand((CardGroup) object, result);
			} else if (object instanceof ICard) {
				result.add((ICard) object);
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
		} else if (element instanceof Collection) {
			return ((Collection) element).size() > 0;
		} else if (element instanceof Object[]) {
			return ((Object[]) element).length > 0;
		}
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}
}