/**
 *
 */
package com.reflexit.magiccards.ui.views.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;

import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

/**
 * Flat contents of the object with one root.
 * 
 * @author elaskavaia
 *
 */
public class GroupExpandContentProvider implements ITreeContentProvider, ISizeContentProvider, ISelectionTranslator {
	private Object input;
	private String top;
	private Object[] topChildren;

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// if (this.input == newInput)
		// return;
		Object[] res = calculateChildren(newInput);
		this.input = newInput;
		this.top = "Cards";
		this.topChildren = res;
		// viewer.refresh();
	}

	@Override
	public Object[] getChildren(Object element) {
		if (element == input) {
			return new Object[] { top };
		}
		if (element == top) {
			return topChildren;
		}
		return calculateChildren(element);
	}

	public Object[] calculateChildren(Object element) {
		Object[] res;
		if (element instanceof ICardGroup) {
			ICardGroup group = (ICardGroup) element;
			Collection<ICard> children = daexpand(group, new ArrayList<>());
			res = children.toArray(new Object[children.size()]);
		} else if (element instanceof ICard) {
			res = new Object[0];
		} else if (element instanceof IFilteredCardStore) {
			ICardGroup root = ((IFilteredCardStore<?>) element).getCardGroupRoot();
			res = calculateChildren(root);
		} else if (element instanceof Collection) {
			Collection<ICard> list = daexpand((Collection<?>) element, new ArrayList<>());
			res = list.toArray(new Object[list.size()]);
		} else if (element instanceof Object[]) {
			res = (Object[]) element;
		} else {
			res = new Object[0];
		}
		return res;
	}

	protected Collection<ICard> daexpand(Collection<?> collection, List<ICard> result) {
		for (Object object : collection) {
			if (object instanceof ICardGroup) {
				daexpand((ICardGroup) object, result);
			} else if (object instanceof ICard) {
				result.add((ICard) object);
			}
		}
		return result;
	}

	protected Collection<ICard> daexpand(ICardGroup cardGroup, List<ICard> result) {
		return daexpand(cardGroup.getChildrenList(), result);
	}

	@Override
	public Object getParent(Object element) {
		if (element == input) {
			return null;
		}
		if (element == top) {
			return input;
		}
		return top;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element == input) {
			return true;
		}
		if (element instanceof ICardGroup) {
			return ((ICardGroup) element).size() > 0;
		} else if (element instanceof ICard) {
			return false;
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
	public int getSize(Object element) {
		if (element instanceof ICardGroup) {
			return ((ICardGroup) element).size();
		} else if (element instanceof ICard) {
			return 0;
		} else if (element instanceof IFilteredCardStore) {
			IFilteredCardStore fstore = (IFilteredCardStore) element;
			return fstore.getCardGroupRoot().size();
		} else if (element instanceof Collection) {
			return ((Collection) element).size();
		} else if (element instanceof Object[]) {
			return ((Object[]) element).length;
		}
		return 0;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public IStructuredSelection translateSelection(IStructuredSelection selection, int level) {
		if (selection.isEmpty() || input == null)
			return selection;
		ArrayList<Object> res = new ArrayList<>();
		for (Object object : selection.toArray()) {
			Object toSearch = object;
			if (object instanceof TreePath) {
				toSearch = ((TreePath) object).getLastSegment();
			}
			Object found = findTreePath(toSearch, level);
			if (found != null)
				res.add(found);
		}
		return new StructuredSelection(res);
	}

	protected Object findTreePath(Object toSearch, int levelTruncate) {
		if (toSearch == input)
			return new TreePath(new Object[0]);
		if (toSearch == top)
			return new TreePath(new Object[] { top });
		if (toSearch instanceof ICard && !(toSearch instanceof ICardGroup))
			return new TreePath(new Object[] { top, toSearch });
		return toSearch;
	}
}