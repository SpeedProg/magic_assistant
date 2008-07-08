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
		store.setDefault(PreferenceConstants.MDBVIEW_COLS, "1,-2,3,4,5,6,-7,-8,-9,-10");
		store.setDefault(PreferenceConstants.LIBVIEW_COLS, "1,-2,3,4,5,6,-7,-8,-9,-10,11");
		store.setDefault(PreferenceConstants.DECKVIEW_COLS, "1,-2,3,4,-5,-6,-7,-8,-9,-10,11");
	}
}
