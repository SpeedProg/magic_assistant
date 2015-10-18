package com.reflexit.magiccards.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.service.prefs.BackingStoreException;

import com.reflexit.magiccards.core.FileUtils;
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
		IPreferenceStore storeCore = MagicUIActivator.getDefault().getCorePreferenceStore();
		storeCore.setDefault(PreferenceConstants.DIR_MAGICCARDS, FileUtils.MAGICCARDS);
		storeCore.setDefault(PreferenceConstants.DIR_BACKUP, FileUtils.BACKUP);
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
		store.setDefault(PreferenceConstants.PRICE_PROVIDER, PriceProviderManager.getInstance()
				.getDefaultProvider().getName());
		store.setDefault(PreferenceConstants.LAST_SELECTION, 205961);
		// local settings
		getMdbStore()
				.setDefault(
						PreferenceConstants.LOCAL_COLUMNS,
						"Name,-Card Id,Cost,Type,Power,Toughness,-Oracle Text,Set,-Rarity,-Color Type,-Color,-Online Price,-Artist,-Rating,-Collector's Number,-Language,-Text");
		getLibStore()
				.setDefault(
						PreferenceConstants.LOCAL_COLUMNS,
						"Name,-Card Id,Cost,Type,Power,Toughness,-Oracle Text,-Set,-Rarity,-Color Type,Count,Location,-Color,-Ownership,-Comment,-User Price,-Online Price,-Artist,-Rating,-For Trade,-Special,-Collector's Number,-Language,-Text");
		getDeckStore()
				.setDefault(
						PreferenceConstants.LOCAL_COLUMNS,
						"Name,-Card Id,Cost,Type,Power,Toughness,-Oracle Text,-Set,-Rarity,-Color Type,Count,-Location,-Color,-Ownership,-Comment,-User Price,-Online Price,-Artist,-Rating,-For Trade,-Special,-Collector's Number,-Language,-Text");
		getCollectorStore()
				.setDefault(
						PreferenceConstants.LOCAL_COLUMNS,
						"Group,-Name,Progress,-Progress4,-Card Id,-Cost,-Type,-Power,-Toughness,-Oracle Text,-Text,-Set,-Rarity,-Color Type,-Count,"
								+ "Collector's Number,Artist,Location,-Color,Ownership,User Price,Online Price,-Rating,-For Trade,"
								+ "Comment,Special,-Language");
		getDeckStore().setDefault(PreferenceConstants.LOCAL_SHOW_QUICKFILTER, false);
		getMdbStore().setDefault(PreferenceConstants.LOCAL_SHOW_QUICKFILTER, true);
		getLibStore().setDefault(PreferenceConstants.LOCAL_SHOW_QUICKFILTER, true);
		getCollectorStore().setDefault(PreferenceConstants.LOCAL_SHOW_QUICKFILTER, false);
	}

	public static IPreferenceStore getGlobalStore() {
		return MagicUIActivator.getDefault().getPreferenceStore();
	}

	public static IPersistentPreferenceStore getLocalStore(String id) {
		if (id == null)
			id = MagicUIActivator.PLUGIN_ID;
		ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, id);
		return store;
	}

	public static IEclipsePreferences getPreferences(String id) {
		if (id == null)
			id = MagicUIActivator.PLUGIN_ID;
		return InstanceScope.INSTANCE.getNode(id);
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

	public static void setToDefault(IPreferenceStore store) {
		String[] preferenceNames = preferenceNames(store);
		for (String id : preferenceNames) {
			store.setToDefault(id);
		}
	}

	public static String[] preferenceNames(IPreferenceStore store) {
		String res[] = null;
		if (store instanceof PreferenceStore) {
			res = ((PreferenceStore) store).preferenceNames();
		} else if (store instanceof ScopedPreferenceStore) {
			IEclipsePreferences[] preferenceNodes = ((ScopedPreferenceStore) store).getPreferenceNodes(false);
			try {
				if (preferenceNodes.length > 0)
					res = preferenceNodes[0].keys();
			} catch (BackingStoreException e) {
				// res = null;
			}
		}
		if (res == null)
			return null;
		return res;
	}
}
