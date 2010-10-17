package com.reflexit.magiccards.ui.views.printings;

import java.util.Collection;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;

public class PrintingsContentProvider implements ITreeContentProvider {
	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public Object[] getChildren(Object element) {
		if (element instanceof CardGroup) {
			Collection<IMagicCard> children = ((CardGroup) element).getChildren();
			return children.toArray(new Object[children.size()]);
		} else if (element instanceof Collection) {
			Collection children = (Collection) element;
			return children.toArray(new Object[children.size()]);
		}
		return null;
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof CardGroup) {
			Collection<IMagicCard> children = ((CardGroup) element).getChildren();
			return children.size() > 0;
		} else if (element instanceof Collection) {
			Collection children = (Collection) element;
			return children.size() > 0;
		}
		return false;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}
}
