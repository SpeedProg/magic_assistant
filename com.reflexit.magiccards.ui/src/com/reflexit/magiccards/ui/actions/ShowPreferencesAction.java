package com.reflexit.magiccards.ui.actions;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class ShowPreferencesAction extends ImageAction {
	private String id;

	public ShowPreferencesAction(String id) {
		super("Preferences...", "icons/clcl16/gear.png", "Opens UI preferences");
		this.id = id;
		// setId(ActionFactory.DELETE.getId());
		// setActionDefinitionId("org.eclipse.ui.edit.findReplace");
	}

	@Override
	public void run() {
		String id = getPreferencePageId();
		if (id != null) {
			before();
			PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(getShell(), id, new String[] { id },
					null);
			if (dialog.open() == Window.OK) {
				after();
			}
		}
	}

	public String getPreferencePageId() {
		return id;
	}

	public void after() {
		// hook
	}

	public void before() {
		// hook
	}
}
