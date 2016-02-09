package com.reflexit.magiccards.ui.views.editions;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.reflexit.magiccards.core.model.Edition;
import com.reflexit.magiccards.core.model.Editions;

public class EditionsContentProvider implements ITreeContentProvider {
	ArrayList<Edition> editions = new ArrayList<Edition>();

	@Override
	public void dispose() {
		editions.clear();
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof Editions) {
			this.editions.clear();
			this.editions.addAll(((Editions) newInput).getEditions());
		}
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		return new Object[0];
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof Editions) {
			return true;
		}
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return this.editions.toArray();
	}
}
