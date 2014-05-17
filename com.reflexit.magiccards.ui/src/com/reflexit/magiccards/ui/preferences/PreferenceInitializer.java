package com.reflexit.magiccards.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.reflexit.magiccards.ui.MagicUIActivator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
	private static IPreferenceStore deckStore;
	private static IPreferenceStore libStore;
	private static IPreferenceStore mdbStore;
	private static IPreferenceStore collectorStore;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = getGlobalStore();
		store.setDefault(PreferenceConstants.GATHERER_SITE, "http://ww2.wizards.com/gatherer");
		store.setDefault(PreferenceConstants.GATHERER_UPDATE,
				"http://ww2.wizards.com/gatherer/index.aspx?output=Spoiler&setfilter=Standard");
		store.setDefault(PreferenceConstants.CACHE_IMAGES, true);
		store.setDefault(PreferenceConstants.LOAD_IMAGES, true);
		store.setDefault(PreferenceConstants.LOAD_RULINGS, false);
		store.setDefault(PreferenceConstants.LOAD_EXTRAS, false);
		store.setDefault(PreferenceConstants.LOAD_PRINTINGS, false);
		store.setDefault(PreferenceConstants.SHOW_GRID, false);
		store.setDefault(PreferenceConstants.CHECK_FOR_UPDATES, true);
		store.setDefault(PreferenceConstants.CHECK_FOR_CARDS, true);
		store.setDefault(PreferenceConstants.OWNED_COPY, false);
		store.setDefault(PreferenceConstants.CURRENCY, "USD");
		store.setDefault(PreferenceConstants.WORK_OFFLINE, false);
		store.setDefault(PreferenceConstants.PRICE_PROVIDER, PriceProviderManager.getInstance().getDefaultProvider().getName());
		// local settings
		getMdbStore()
				.setDefault(
						PreferenceConstants.LOCAL_COLUMNS,
						"Name,-Card Id,Cost,Type,Power,Toughness,-Oracle Text,Set,-Rarity,-Color Type,-Color,-Seller Price,-Artist,-Rating,-Collector's Number,-Language,-Text");
		getLibStore()
				.setDefault(
						PreferenceConstants.LOCAL_COLUMNS,
						"Name,-Card Id,Cost,Type,Power,Toughness,-Oracle Text,-Set,-Rarity,-Color Type,Count,Location,-Color,-Ownership,-Comment,-Price,-Seller Price,-Artist,-Rating,-For Trade,-Special,-Collector's Number,-Language,-Text");
		getDeckStore()
				.setDefault(
						PreferenceConstants.LOCAL_COLUMNS,
						"Name,-Card Id,Cost,Type,Power,Toughness,-Oracle Text,-Set,-Rarity,-Color Type,Count,-Location,-Color,-Ownership,-Comment,-Price,-Seller Price,-Artist,-Rating,-For Trade,-Special,-Collector's Number,-Language,-Text");
		getCollectorStore().setDefault(
				PreferenceConstants.LOCAL_COLUMNS,
				"Group,-Name,Progress,-Progress4,-Card Id,-Cost,-Type,-Power,-Toughness,-Oracle Text,-Text,-Set,-Rarity,-Color Type,-Count,"
						+ "Collector's Number,Artist,Location,-Color,Ownership,Price,Seller Price,-Rating,-For Trade,"
						+ "Comment,Special,-Language");
		getDeckStore().setDefault(PreferenceConstants.LOCAL_SHOW_QUICKFILTER, false);
		getMdbStore().setDefault(PreferenceConstants.LOCAL_SHOW_QUICKFILTER, true);
		getLibStore().setDefault(PreferenceConstants.LOCAL_SHOW_QUICKFILTER, true);
		getCollectorStore().setDefault(PreferenceConstants.LOCAL_SHOW_QUICKFILTER, false);
	}

	public static IPreferenceStore getGlobalStore() {
		return MagicUIActivator.getDefault().getPreferenceStore();
	}

	public static PrefixedPreferenceStore getLocalStore(String id) {
		return new PrefixedPreferenceStore(MagicUIActivator.getDefault().getPreferenceStore(), id);
	}

	public static IPreferenceStore getDeckStore() {
		if (deckStore == null)
			deckStore = getLocalStore(DeckViewPreferencePage.class.getName());
		return deckStore;
	}

	public static IPreferenceStore getLibStore() {
		if (libStore == null)
			libStore = getLocalStore(LibViewPreferencePage.class.getName());
		return libStore;
	}

	public static IPreferenceStore getMdbStore() {
		if (mdbStore == null)
			mdbStore = getLocalStore(MagicDbViewPreferencePage.class.getName());
		return mdbStore;
	}

	public static IPreferenceStore getCollectorStore() {
		if (collectorStore == null)
			collectorStore = getLocalStore(CollectorViewPreferencePage.class.getName());
		return collectorStore;
	}
}
