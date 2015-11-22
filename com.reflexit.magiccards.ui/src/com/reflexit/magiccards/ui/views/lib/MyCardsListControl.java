package com.reflexit.magiccards.ui.views.lib;

import java.util.Collection;

import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.GroupOrder;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.commands.ShowFilterHandler;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.IMagicColumnViewer;
import com.reflexit.magiccards.ui.views.SplitViewer;

public abstract class MyCardsListControl extends AbstractMagicCardsListControl {
	public MyCardsListControl(AbstractCardsView abstractCardsView) {
		super(abstractCardsView);
	}

	@Override
	public IMagicColumnViewer createViewer(Composite parent) {
		return new SplitViewer(parent, getPreferencePageId());
	}

	@Override
	protected String getPreferencePageId() {
		return getViewPreferencePageId();
	}

	@Override
	public Collection<GroupOrder> getGroups() {
		Collection<GroupOrder> res = super.getGroups();
		res.add(new GroupOrder(MagicCardField.LOCATION));
		return res;
	}

	@Override
	protected void runShowFilter() {
		if (ShowFilterHandler.execute()) {
			reloadData();
		}
		// CardFilter.open(getViewSite().getShell());
		// MyCardsFilterDialog cardFilterDialog = new
		// MyCardsFilterDialog(getShell(),
		// getFilterPreferenceStore());
		// if (cardFilterDialog.open() == IStatus.OK)
		// reloadData();
	}

	@Override
	protected abstract IFilteredCardStore<ICard> doGetFilteredStore();
}
