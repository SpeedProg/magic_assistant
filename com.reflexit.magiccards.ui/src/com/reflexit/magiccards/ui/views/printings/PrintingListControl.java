package com.reflexit.magiccards.ui.views.printings;

import org.eclipse.jface.action.MenuManager;

import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.views.AbstractCardsView;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.IMagicColumnViewer;

public class PrintingListControl extends AbstractMagicCardsListControl {
	private IMagicCard card;

	public PrintingListControl(AbstractCardsView abstractCardsView) {
		super(abstractCardsView);
	}

	@Override
	public IMagicColumnViewer createViewerManager() {
		return new PrintingsManager(getPreferencePageId());
	}

	@Override
	protected MenuManager createGroupMenu() {
		MenuManager groupMenu = new MenuManager("Group By");
		groupMenu.add(new GroupAction("None", null));
		groupMenu.add(new GroupAction("Set", MagicCardField.SET));
		groupMenu.add(new GroupAction("Location", MagicCardFieldPhysical.LOCATION));
		groupMenu.add(new GroupAction("Ownership", MagicCardFieldPhysical.OWNERSHIP));
		return groupMenu;
	}

	@Override
	protected void updateStatus() {
		if (card != MagicCard.DEFAULT && card != null)
			setStatus(card.getName() + ": " + getStatusMessage());
		else
			super.updateStatus();
	}

	@Override
	protected void sort(int index) {
		updateSortColumn(index);
		updateViewer();
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
		if (isDbMode()) {
			if (totalSize == 1)
				return "Only one version found";
			return "Total " + totalSize + " diffrent versions";
		} else {
			String s = "";
			if (count != 1)
				s = "s";
			return "Total " + count + " card" + s + " in your collections";
		}
	}

	boolean isDbMode() {
		return ((PrintingsManager) manager).isDbMode();
	}

	@Override
	public void updateGroupBy(ICardField field) {
		if (((PrintingsManager) manager).isDbMode())
			return;
		super.updateGroupBy(field);
	}

	public void updateDbMode(boolean mode) {
		((PrintingsManager) manager).updateDbMode(mode);
		if (mode)
			updateGroupBy(null);
	}

	public void setCard(IMagicCard card) {
		this.card = card;
	}
}
