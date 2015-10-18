package com.reflexit.magiccards.ui.views;

import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class RootTreeViewContentProvider extends TreeViewContentProvider {
	@Override
	public Object[] getChildren(Object element) {
		if (element instanceof IFilteredCardStore) {
			ICardGroup root = ((IFilteredCardStore) element).getCardGroupRoot();
			return new Object[] { root };
		} else {
			return super.getChildren(element);
		}
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IFilteredCardStore) {
			return true;
		}
		return super.hasChildren(element);
	}
}
