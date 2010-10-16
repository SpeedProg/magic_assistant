package com.reflexit.magiccards.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.reflexit.magiccards.ui.MagicUIActivator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = MagicUIActivator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.GATHERER_SITE, "http://ww2.wizards.com/gatherer");
		store.setDefault(PreferenceConstants.GATHERER_UPDATE,
		        "http://ww2.wizards.com/gatherer/index.aspx?output=Spoiler&setfilter=Standard");
		store.setDefault(
		        PreferenceConstants.MDBVIEW_COLS,
		        "Name,-Card Id,Cost,Type,Power,Toughness,-Oracle Text,Set,Rarity,-Color Type,-Color,-Seller Price,-Artist,-Rating,-Collector's Number");
		store.setDefault(
		        PreferenceConstants.LIBVIEW_COLS,
		        "Name,-Card Id,Cost,Type,Power,Toughness,-Oracle Text,-Set,-Rarity,-Color Type,Count,Location,-Color,-Ownership,-Comment,-Price,-Seller Price,-Artist,-Rating,-For Trade,-Special,-Collector's Number");
		store.setDefault(
		        PreferenceConstants.DECKVIEW_COLS,
		        "Name,-Card Id,Cost,Type,Power,Toughness,-Oracle Text,-Set,-Rarity,-Color Type,Count,-Location,-Color,-Ownership,-Comment,-Price,-Seller Price,-Artist,-Rating,-For Trade,-Special,-Collector's Number");
		store.setDefault(PreferenceConstants.CACHE_IMAGES, true);
		store.setDefault(PreferenceConstants.LOAD_IMAGES, true);
		store.setDefault(PreferenceConstants.LOAD_RULINGS, true);
		store.setDefault(PreferenceConstants.SHOW_GRID, false);
	}
}
