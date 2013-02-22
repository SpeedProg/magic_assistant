package com.reflexit.magiccards.ui.views.lib;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.MenuManager;

import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.dialogs.MyCardsFilterDialog;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.CompositeViewerManager;
import com.reflexit.magiccards.ui.views.IMagicColumnViewer;

public abstract class MyCardsListControl extends AbstractMagicCardsListControl {
	public MyCardsListControl(AbstractCardsView abstractCardsView) {
		super(abstractCardsView);
	}

	@Override
	public IMagicColumnViewer createViewerManager() {
		return new CompositeViewerManager(getPreferencePageId());
	}

	@Override
	protected MenuManager createGroupMenu() {
		MenuManager x = super.createGroupMenu();
		x.add(createGroupAction(MagicCardFieldPhysical.LOCATION));
		return x;
	}

	@Override
	protected void runShowFilter() {
		// CardFilter.open(getViewSite().getShell());
		MyCardsFilterDialog cardFilterDialog = new MyCardsFilterDialog(getShell(), getFilterPreferenceStore());
		if (cardFilterDialog.open() == IStatus.OK)
			reloadData();
	}

	@Override
	protected abstract IFilteredCardStore<ICard> doGetFilteredStore();
}
