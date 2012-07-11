package com.reflexit.magiccards.ui.views.collector;

import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.MenuManager;

import com.reflexit.magiccards.core.model.FilterHelper;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.dialogs.MyCardsFilterDialog;
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
		groupMenu.add(new GroupAction("Artist", MagicCardField.ARTIST));
		return groupMenu;
	}

	@Override
	protected void createGroupAction() {
		// super.createGroupAction();
	}

	@Override
	protected void runShowFilter() {
		MyCardsFilterDialog cardFilterDialog = new MyCardsFilterDialog(getShell(), getLocalPreferenceStore());
		if (cardFilterDialog.open() == IStatus.OK)
			reloadData();
	}

	@Override
	protected void initManager() {
		getLocalPreferenceStore().setDefault(FilterHelper.GROUP_FIELD, MagicCardField.SET.toString());
		super.initManager();
	}

	public int getOwnSize(Iterable children) {
		int count = 0;
		for (Iterator iterator = children.iterator(); iterator.hasNext();) {
			Object object = iterator.next();
			if (object instanceof MagicCardPhysical) {
				if (((MagicCardPhysical) object).isOwn())
					count++;
			}
		}
		return count;
	}

	@Override
	public String getStatusMessage() {
		IFilteredCardStore filteredStore = getFilteredStore();
		if (filteredStore == null)
			return "";
		ICardStore cardStore = filteredStore.getCardStore();
		return "Total " + getOwnSize(cardStore) + " unique card(s) in your collections";
	}

	@Override
	public void handleEvent(final CardEvent event) {
		int type = event.getType();
		if (type == CardEvent.UPDATE || type == CardEvent.REMOVE || type == CardEvent.ADD) {
			reloadData();
		}
	}
}
