package com.reflexit.magiccards.ui.views.editions;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Editions.Edition;

public class EditionsContentProvider implements ITreeContentProvider {
	ArrayList<Edition> editions = new ArrayList<Edition>();

	public void dispose() {
		editions.clear();
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof Editions) {
			this.editions.clear();
			this.editions.addAll(((Editions) newInput).getEditions());
		}
	}

	public Object[] getChildren(Object parentElement) {
		return new Object[0];
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof Editions) {
			return true;
		}
		return false;
	}

	public Object[] getElements(Object inputElement) {
		return this.editions.toArray();
	}
}
