package com.reflexit.magiccards.ui.views.lib;

import org.eclipse.core.runtime.IStatus;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.dialogs.DeckFilterDialog;
import com.reflexit.magiccards.ui.views.AbstractCardsView;

public class DeckListControl extends MyCardsListControl {
	public DeckListControl(AbstractCardsView abstractCardsView) {
		super(abstractCardsView);
	}

	@Override
	protected synchronized void updateStatus() {
		setStatus(getStatusMessage());
	}

	@Override
	protected void runShowFilter() {
		DeckFilterDialog cardFilterDialog = new DeckFilterDialog(getShell(), getLocalPreferenceStore());
		if (cardFilterDialog.open() == IStatus.OK)
			reloadData(); // was null in realoadData
	}

	@Override
	public IFilteredCardStore doGetFilteredStore() {
		String secondaryId = abstractCardsView.getViewSite().getSecondaryId();
		return DataManager.getCardHandler().getCardCollectionFilteredStore(secondaryId);
	}
}
