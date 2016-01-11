package com.reflexit.magiccards.ui.views.lib;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.preference.IPreferenceStore;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.Locations;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.LibViewPreferencePage;
import com.reflexit.magiccards.ui.utils.WaitUtils;
import com.reflexit.magiccards.ui.views.AbstractMagicCardsListControl;
import com.reflexit.magiccards.ui.views.IMagicControl;
import com.reflexit.magiccards.ui.views.ViewPageContribution;
import com.reflexit.magiccards.ui.views.analyzers.AbstractMagicControlViewPage;
import com.reflexit.magiccards.ui.views.lib.MyCardsListControl.Presentation;

public class MyCardsView extends AbstractMyCardsView {
	public static final String ID = "com.reflexit.magiccards.ui.views.lib.MyCardsView";
	private AbstractMagicControlViewPage page;

	public MyCardsView() {
	}

	@Override
	protected void createPages() {
		page = new AbstractMagicControlViewPage() {
			@Override
			public AbstractMagicCardsListControl doGetMagicCardListControl() {
				return createViewControl();
			}
		};
		getPageGroup().add(new ViewPageContribution("", "Groups", null, page));
	}

	protected MyCardsListControl createViewControl() {
		return new MyCardsListControl(this, Presentation.SPLITTREE) {
			@Override
			public IFilteredCardStore doGetFilteredStore() {
				return DataManager.getCardHandler().getLibraryFilteredStore();
			}

			@Override
			protected void runDoubleClick() {
				MyCardsView.this.runDoubleClick();
			}
		};
	}

	@Override
	protected IMagicControl getMagicControl() {
		return page.getMagicControl();
	}

	@Override
	public String getHelpId() {
		return MagicUIActivator.helpId("viewcol");
	}

	@Override
	protected void loadInitialInBackground() {
		super.loadInitialInBackground();
		reloadData();
	}

	@Override
	protected String getPreferencePageId() {
		return LibViewPreferencePage.class.getName();
	}

	public void setLocationFilter(Location loc) {
		getMagicControl().setStatus("Loading " + loc + "...");
		WaitUtils.scheduleJob("Updating location", () -> {
			IPreferenceStore preferenceStore = getLocalPreferenceStore();
			Collection ids = Locations.getInstance().getIds();
			String locId = Locations.getInstance().getPrefConstant(loc);
			for (Iterator iterator = ids.iterator(); iterator.hasNext();) {
				String id = (String) iterator.next();
				if (id.startsWith(locId + ".") || id.startsWith(locId + "/") || id.equals(locId)
						|| id.equals(locId + Location.SIDEBOARD_SUFFIX)) {
					preferenceStore.setValue(id, true);
				} else {
					preferenceStore.setValue(id, false);
				}
			}
			reloadData();
		});
	}

	@Override
	public String getId() {
		return ID;
	}
}