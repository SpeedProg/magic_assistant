package com.reflexit.magiccards.ui.views.collector;

import org.eclipse.jface.action.MenuManager;

import com.reflexit.magiccards.core.model.FilterHelper;
import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.IMagicColumnViewer;

public class CollectorListControl extends AbstractMagicCardsListControl {
	public CollectorListControl(AbstractCardsView abstractCardsView) {
		super(abstractCardsView);
	}

	@Override
	public IMagicColumnViewer createViewerManager() {
		return new CollectorManager(getPreferencePageId());
	}

	@Override
	protected MenuManager createGroupMenu() {
		MenuManager groupMenu = new MenuManager("Group By");
		groupMenu.add(new GroupAction("Set", MagicCardField.SET));
		groupMenu.add(new GroupAction("Location", MagicCardFieldPhysical.LOCATION));
		groupMenu.add(new GroupAction("Ownership", MagicCardFieldPhysical.OWNERSHIP));
		return groupMenu;
	}

	@Override
	protected void sort(int index) {
		updateSortColumn(index);
		updateViewer();
	}

	@Override
	protected void initManager() {
		getLocalPreferenceStore().setDefault(FilterHelper.GROUP_FIELD, MagicCardField.SET.toString());
		super.initManager();
	}

	@Override
	public String getStatusMessage() {
		IFilteredCardStore filteredStore = getFilteredStore();
		if (filteredStore == null)
			return "";
		ICardStore cardStore = filteredStore.getCardStore();
		int totalSize = cardStore.size();
		int count = totalSize;
		if (cardStore instanceof ICardCountable) {
			count = ((ICardCountable) cardStore).getCount();
		}
		{
			String s = "";
			if (count != 1)
				s = "s";
			return "Total " + totalSize + " unique card" + s + " in your collections";
		}
	}

	@Override
	public void updateGroupBy(ICardField field) {
		super.updateGroupBy(field);
	}

	@Override
	public void handleEvent(final CardEvent event) {
		int type = event.getType();
		if (type == CardEvent.UPDATE || type == CardEvent.REMOVE) {
			reloadData();
		}
	}
}
