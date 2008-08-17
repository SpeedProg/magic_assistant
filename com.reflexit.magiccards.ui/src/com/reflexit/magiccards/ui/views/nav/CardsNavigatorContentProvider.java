package com.reflexit.magiccards.ui.views.nav;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import java.util.Collection;

import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;

public class CardsNavigatorContentProvider implements ITreeContentProvider {
	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public Object[] getChildren(Object element) {
		if (element instanceof CardOrganizer) {
			Collection children = ((CardOrganizer) element).getChildren();
			return children.toArray(new Object[children.size()]);
		} else if (element instanceof Collection) {
			Collection children = (Collection) element;
			return children.toArray(new Object[children.size()]);
		}
		return null;
	}

	public Object getParent(Object element) {
		if (element instanceof CardElement) {
			return ((CardElement) element).getParent();
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof CardOrganizer) {
			return ((CardOrganizer) element).hasChildren();
		} else if (element instanceof Collection) {
			Collection children = (Collection) element;
			return children.size() > 0;
		}
		return false;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public static ViewerFilter getContainerFilter() {
		return new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				return element instanceof CardOrganizer;
			}
		};
	}
}
