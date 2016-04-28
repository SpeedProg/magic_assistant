package com.reflexit.magiccards.ui.views.lib;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.views.Presentation;

public class DeckListControl extends MyCardsListControl implements IDeckPage {
	public DeckListControl() {
	}

	public DeckListControl(Presentation pres) {
		super(pres);
	}

	@Override
	public IFilteredCardStore doGetFilteredStore() {
		String secondaryId = getViewSite().getSecondaryId();
		return DM.getCardHandler().getCardCollectionFilteredStore(secondaryId);
	}

	@Override
	public IPersistentPreferenceStore getElementPreferenceStore() {
		return new ScopedPreferenceStore(InstanceScope.INSTANCE, getPreferencePageId() + ".deck." + getName());
	}
}
