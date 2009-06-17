package com.reflexit.magiccards.ui.views.lib;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.SWT;

import java.util.Collection;
import java.util.Iterator;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.Locations;
import com.reflexit.magiccards.core.model.events.ICardEventListener;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.dialogs.CardFilterDialog2;
import com.reflexit.magiccards.ui.preferences.LibViewPreferencePage;
import com.reflexit.magiccards.ui.preferences.LocationFilterPreferencePage;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;

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
	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
	}

	@Override
	public IFilteredCardStore doGetFilteredStore() {
		return DataManager.getCardHandler().getMyCardsHandler();
	}

	@Override
	protected String getPreferencePageId() {
		return LibViewPreferencePage.class.getName();
	}

	@Override
	protected String getPrefenceColumnsId() {
		return PreferenceConstants.LIBVIEW_COLS;
	}

	@Override
	protected void runShowFilter() {
		// CardFilter.open(getViewSite().getShell());
		CardFilterDialog2 cardFilterDialog = new CardFilterDialog2(getShell(), getPreferenceStore());
		cardFilterDialog.addNode(new PreferenceNode("locations", new LocationFilterPreferencePage(SWT.MULTI)));
		if (cardFilterDialog.open() == IStatus.OK)
			this.manager.loadData(null);
	}

	/**
	 * @param preferenceStore
	 * @param portableString
	 */
	public void setLocationFilter(String loc) {
		IPreferenceStore preferenceStore = getPreferenceStore();
		Collection ids = Locations.getInstance().getIds();
		String locId = Locations.getInstance().getPrefConstant(loc);
		for (Iterator iterator = ids.iterator(); iterator.hasNext();) {
			String id = (String) iterator.next();
			if (id.startsWith(locId)) {
				preferenceStore.setValue(id, true);
			} else {
				preferenceStore.setValue(id, false);
			}
		}
		reloadData();
	}
}