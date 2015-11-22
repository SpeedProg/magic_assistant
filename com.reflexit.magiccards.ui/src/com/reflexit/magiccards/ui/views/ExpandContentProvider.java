/**
 *
 */
package com.reflexit.magiccards.ui.views;

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
 * Flat contents of the object. Expanded up "Name" groups.
 * 
 * @author elaskavaia
 *
 */
public class ExpandContentProvider implements ITreeContentProvider, ISizeContentProvider {
	private Object input;
	private Object[] topChildren;

	@Override
	public void dispose() {
		topChildren = null;
		input = null;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (this.input == newInput)
			return;
		this.input = newInput;
		this.topChildren = getFlatChildren(newInput);
	}

	@Override
	public Object[] getChildren(Object element) {
		if (element == input) {
			return topChildren;
		}
		return new Object[0];
	}

	public Object[] getFlatChildren(Object element) {
		Object[] res = null;
		if (element instanceof CardGroup) {
			CardGroup group = (CardGroup) element;
			Collection<ICard> children = daexpand(group);
			res = children.toArray(new Object[children.size()]);
		} else if (element instanceof IFilteredCardStore) {
			ICardGroup root = ((IFilteredCardStore<?>) element).getCardGroupRoot();
			res = getFlatChildren(root);
		} else if (element instanceof Collection) {
			Collection<ICard> list = daexpand((Collection<?>) element);
			res = list.toArray(new Object[list.size()]);
		} else if (element instanceof Object[]) {
			return (Object[]) element;
		}
		if (res == null)
			return new Object[0];
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
		return getSize(element) > 0;
	}

	@Override
	public int getSize(Object element) {
		if (element == input) {
			return topChildren.length;
		}
		return 0;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}
}