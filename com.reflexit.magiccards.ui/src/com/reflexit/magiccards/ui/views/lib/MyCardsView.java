package com.reflexit.magiccards.ui.views.lib;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.Locations;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.LibViewPreferencePage;
import com.reflexit.magiccards.ui.utils.WaitUtils;

public class MyCardsView extends AbstractMyCardsView implements ICardEventListener {
	public static final String ID = "com.reflexit.magiccards.ui.views.lib.LibView";

	/**
	 * The constructor.
	 */
	public MyCardsView() {
	}

	@Override
	protected void makeActions() {
		super.makeActions();
	}

	@Override
	public String getHelpId() {
		return MagicUIActivator.helpId("viewcol");
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
	}

	@Override
	protected void createMainControl(Composite parent) {
		super.createMainControl(parent);
		control.setStatus("Loading ...");
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
	}

	@Override
	protected void loadInitialInBackground() {
		super.loadInitialInBackground();
		reloadData();
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
	}

	@Override
	protected MyCardsListControl doGetViewControl() {
		return new MyCardsListControl(this) {
			@Override
			public IFilteredCardStore doGetFilteredStore() {
				return DataManager.getCardHandler().getLibraryFilteredStore();
			}
		};
	}

	@Override
	protected String getPreferencePageId() {
		return LibViewPreferencePage.class.getName();
	}

	public void setLocationFilter(Location loc) {
		control.setStatus("Loading " + loc + "...");
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

	@Override
	public void handleEvent(final CardEvent event) {
		super.handleEvent(event);
		int type = event.getType();
		if (type == CardEvent.ADD_CONTAINER || type == CardEvent.REMOVE_CONTAINER
				|| type == CardEvent.RENAME_CONTAINER) {
			reloadData();
		}
	}
}