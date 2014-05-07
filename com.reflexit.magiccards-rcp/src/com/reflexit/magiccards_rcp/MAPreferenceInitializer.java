package com.reflexit.magiccards_rcp;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.ui.IWorkbenchPreferenceConstants;

public class MAPreferenceInitializer extends AbstractPreferenceInitializer {
	public MAPreferenceInitializer() {
	}

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences node = DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		// API preferences
		node.put(MAWorkbenchPreferences.PROJECT_OPEN_NEW_PERSPECTIVE, IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE);
		// Set the workspace selection dialog to open by default
		node.putBoolean(MAWorkbenchPreferences.SHOW_WORKSPACE_SELECTION_DIALOG, true);
	}
}
