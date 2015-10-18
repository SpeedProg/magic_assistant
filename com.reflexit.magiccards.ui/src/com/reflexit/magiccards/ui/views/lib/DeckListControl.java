package com.reflexit.magiccards.ui.views.lib;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.dialogs.DeckFilterDialog;
import com.reflexit.magiccards.ui.views.AbstractCardsView;

public class DeckListControl extends MyCardsListControl {
	private IPersistentPreferenceStore filterStore;

	public DeckListControl(AbstractCardsView abstractCardsView) {
		super(abstractCardsView);
		filterStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, getPreferencePageId() + ".deck." + getName());
	}

	@Override
	protected void runShowFilter() {
		DeckFilterDialog cardFilterDialog = new DeckFilterDialog(getShell(), getFilterPreferenceStore());
		if (cardFilterDialog.open() == IStatus.OK)
			reloadData(); // was null in realoadData
	}

	@Override
	public IPersistentPreferenceStore getFilterPreferenceStore() {
		return filterStore;
	}

	@Override
	public IFilteredCardStore doGetFilteredStore() {
		String secondaryId = abstractCardsView.getViewSite().getSecondaryId();
		return DataManager.getCardHandler().getCardCollectionFilteredStore(secondaryId);
	}
}
