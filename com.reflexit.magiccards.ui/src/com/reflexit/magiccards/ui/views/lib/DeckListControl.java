package com.reflexit.magiccards.ui.views.lib;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.dialogs.DeckFilterDialog;
import com.reflexit.magiccards.ui.preferences.PreferenceInitializer;
import com.reflexit.magiccards.ui.views.AbstractCardsView;

public class DeckListControl extends MyCardsListControl {
	public DeckListControl(AbstractCardsView abstractCardsView, Presentation pres) {
		super(abstractCardsView, pres);
		filterStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, getPreferencePageId() + ".deck." + getName());
		columnsStore = PreferenceInitializer.getLocalStore(getPreferencePageId());
	}

	@Override
	protected void runShowFilter() {
		DeckFilterDialog cardFilterDialog = new DeckFilterDialog(getShell(), getFilterPreferenceStore());
		if (cardFilterDialog.open() == IStatus.OK)
			reloadData(); // was null in realoadData
	}

	@Override
	public IFilteredCardStore doGetFilteredStore() {
		String secondaryId = abstractCardsView.getViewSite().getSecondaryId();
		return DM.getCardHandler().getCardCollectionFilteredStore(secondaryId);
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	protected String getPreferencePageId() {
		String id = super.getPreferencePageId();
		if (id != null && getPresentation() != null) {
			return id + "." + getPresentation().name();
		}
		return id;
	}
}
