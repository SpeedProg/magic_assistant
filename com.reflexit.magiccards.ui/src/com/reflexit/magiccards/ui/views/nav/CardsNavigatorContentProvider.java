package com.reflexit.magiccards.ui.views.nav;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;

public class CardsNavigatorContentProvider implements ITreeContentProvider {
	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
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

	@Override
	public Object getParent(Object element) {
		if (element instanceof CardElement) {
			return ((CardElement) element).getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof CardOrganizer) {
			return ((CardOrganizer) element).hasChildren();
		} else if (element instanceof Collection) {
			Collection children = (Collection) element;
			return children.size() > 0;
		}
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public static String FILTER_NON_FOLDERS = "non_folders";
	public static String FILTER_SIDEBOARDS = "sideboards";

	public static ViewerFilter getFilter(String... strings) {
		Map<String, Object> prop = new HashMap<String, Object>();
		for (int i = 0; i < strings.length; i++) {
			prop.put(strings[i], Boolean.TRUE);
		}
		return getFilter(prop);
	}

	public static ViewerFilter getFilter(final Map<String, Object> prop) {
		return new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof CardElement) {
					return !isFiltered((CardElement) element);
				} else
					return false;
			}

			private boolean isFiltered(CardElement element) {
				if (checkSet(FILTER_SIDEBOARDS)) {
					return element.getLocation().isSideboard();
				}
				if (checkSet(FILTER_NON_FOLDERS)) {
					if (!(element instanceof CardOrganizer)) {
						return true;
					}
				}
				return false;
			}

			private boolean checkSet(String key) {
				Object value = prop.get(key);
				if (value instanceof Boolean)
					return (Boolean) value;
				if (value instanceof String)
					return Boolean.valueOf((String) value);
				return false;
			}
		};
	}

	public static ViewerFilter getContainerFilter() {
		return getFilter(FILTER_NON_FOLDERS);
	}
}
