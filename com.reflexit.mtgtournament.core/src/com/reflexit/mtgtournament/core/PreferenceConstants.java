package com.reflexit.mtgtournament.core;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.osgi.service.prefs.Preferences;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {
	public static final String P_WIN = "winPreference";
	public static final String P_LOOSE = "loosePreference";
	public static final String P_DRAW = "drawPreference";

	public static final Preferences getStore() {
		Preferences node = ConfigurationScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		return node;
	}
}
