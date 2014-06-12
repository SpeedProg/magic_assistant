package com.reflexit.mtgtournament.core;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		// IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		IEclipsePreferences store = DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		store.putInt(PreferenceConstants.P_WIN, 3);
		store.putInt(PreferenceConstants.P_LOOSE, 0);
		store.putInt(PreferenceConstants.P_DRAW, 1);
	}
}
