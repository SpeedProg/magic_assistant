/**
 *
 */
package com.reflexit.magiccards.ui.gallery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

/**
 * Flat contents of the object with one root. Expanded up "Name" groups.
 * 
 * @author elaskavaia
 *
 */
public class GroupExpandContentProvider implements ITreeContentProvider {
	private Object input;
	private String top;
	private Object[] topChildren;

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (this.input == newInput)
			return;
		Object[] res = getChildren(newInput);
		this.input = newInput;
		this.top = "Cards";
		this.topChildren = res;
		// viewer.refresh();
	}

	@Override
	public Object[] getChildren(Object element) {
		Object[] res = null;
		if (element == input) {
			return new Object[] { top };
		}
		if (element == top) {
			return topChildren;
		}
		if (element instanceof CardGroup) {
			CardGroup group = (CardGroup) element;
			Collection<ICard> children = daexpand(group);
			res = children.toArray(new Object[children.size()]);
		} else if (element instanceof IFilteredCardStore) {
			ICardGroup root = ((IFilteredCardStore<?>) element).getCardGroupRoot();
			res = getChildren(root);
		} else if (element instanceof Collection) {
			Collection<ICard> list = daexpand((Collection<?>) element);
			res = list.toArray(new Object[list.size()]);
		} else if (element instanceof Object[]) {
			return (Object[]) element;
		}
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
			if (object instanceof CardGroup && !isLeafGroup((CardGroup) object)) {
				daexpand((CardGroup) object, result);
			} else if (object instanceof ICard) {
				result.add((ICard) object);
			}
		}
		return result;
	}

	protected boolean isLeafGroup(CardGroup group) {
		return group.getFieldIndex() == MagicCardField.NAME;
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element == input) {
			return true;
		}
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