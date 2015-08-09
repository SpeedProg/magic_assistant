package com.reflexit.magiccards.ui.utils;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.viewers.StructuredSelection;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.ILocatable;
import com.reflexit.magiccards.ui.views.AbstractCardsView;

public class MagicAdapterFactory implements IAdapterFactory {
	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (adapterType == ICardStore.class) {
			return (T) adaptToICardStore(adaptableObject);
		}
		return null;
	}

	public static ICardStore adaptToICardStore(Object adaptableObject) {
		if (adaptableObject instanceof ICardStore)
			return (ICardStore) adaptableObject;
		if (adaptableObject instanceof StructuredSelection) {
			StructuredSelection sel = (StructuredSelection) adaptableObject;
			return adaptToICardStore(sel.getFirstElement());
		}
		if (adaptableObject instanceof ILocatable) {
			Location location = ((ILocatable) adaptableObject).getLocation();
			return adaptToICardStore(location);
		}
		if (adaptableObject instanceof Location) {
			return DataManager.getInstance().getCardStore((Location) adaptableObject);
		}
		if (adaptableObject instanceof AbstractCardsView) {
			return ((AbstractCardsView) adaptableObject).getFilteredStore().getCardStore();
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class<?>[] { ICardStore.class };
	}
}
