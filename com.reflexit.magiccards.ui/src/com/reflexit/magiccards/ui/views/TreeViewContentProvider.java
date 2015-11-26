/**
 *
 */
package com.reflexit.magiccards.ui.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class TreeViewContentProvider implements ITreeContentProvider, ISelectionTranslator, ISizeContentProvider {
	public static final Object[] EMPTY_CHILDREN = new Object[] {};
	private int maxCount = 200;
	private Object input;

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.input = newInput;
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
	public int getSize(Object object) {
		return getChildCount(object);
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
			if (root.size() == 0)
				return EMPTY_CHILDREN;
			res = getChildren(root);
		} else {
			return EMPTY_CHILDREN;
		}
		int m = getMaxCount();
		return truncate(res, m);
	}

	private Object[] truncate(Object[] source, int m) {
		if (source.length <= m + 1)
			return source;
		Object o = createOverflow(m, source, m);
		Object[] boo = Arrays.copyOf(source, m + 1);
		boo[m] = o;
		return boo;
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
			Object found = object;
			ArrayList<Object> list = new ArrayList<>();
			list.add(input);
			TreePath path = translatePath(toSearch, list);
			if (path != null)
				found = path;
			if (level >= 1 && found instanceof TreePath) {
				// truncate
				path = (TreePath) found;
				int count = path.getSegmentCount();
				if (count > level) {
					Object segments[] = new Object[level];
					for (int i = 0; i < segments.length; i++) {
						segments[i] = path.getSegment(i);
					}
					found = new TreePath(segments);
				}
			}
			res.add(found);
		}
		return new StructuredSelection(res);
	}

	private TreePath translatePath(Object object, List<Object> parenPath) {
		Object root = parenPath.get(parenPath.size() - 1);
		if (root.equals(object))
			return new TreePath(parenPath.toArray());
		if (parenPath.size() > 20) {
			return null;
		}
		Object children[] = getChildren(root);
		for (Object child : children) {
			if (child == null || root.equals(child))
				continue;
			parenPath.add(child);
			TreePath subPath = translatePath(object, parenPath);
			if (subPath != null) {
				return subPath;
			}
			parenPath.remove(parenPath.size() - 1);
		}
		return null;
	}

	private Object[] chopchop(int startIndex, Object[] source, int max, int depth) {
		int rem = source.length - startIndex;
		if (rem <= max) {
			Object boo[] = new Object[rem];
			System.arraycopy(source, startIndex, boo, 0, rem);
			return boo;
		}
		Object boo[] = new Object[rem / max + 1];
		for (int i = startIndex; i < source.length; i += max) {
			int endIndex = i + max;
			if (endIndex >= source.length) {
				endIndex = source.length - 1;
			}
			int pow = (int) Math.pow(max, depth);
			String name = "[" + i * pow + ".." + endIndex * pow + "]";
			boo[(i - startIndex) / max] = createChopGroup(name, source, i, max);
		}
		if (boo.length <= max)
			return boo;
		return chopchop(0, boo, max, depth + 1);
	}

	protected Object createOverflow(int startIndex, Object[] source, int max) {
		Object res[] = chopchop(startIndex, source, max, 0);
		String name = "Overflow [" + startIndex + ".." + source.length + "]";
		return createChopGroup(name, res, 0, res.length);
	}

	private Object createChopGroup(String name, Object[] source, int sourceIndex, int len) {
		CardGroup g = new CardGroup(null, name);
		for (int i = sourceIndex; i < sourceIndex + len && i < source.length; i++) {
			g.add((ICard) source[i]);
		}
		return g;
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