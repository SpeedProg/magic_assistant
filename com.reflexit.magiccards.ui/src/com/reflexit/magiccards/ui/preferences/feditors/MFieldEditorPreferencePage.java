package com.reflexit.magiccards.ui.preferences.feditors;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;

import com.reflexit.magiccards.ui.preferences.PrefixedPreferenceStore;

public abstract class MFieldEditorPreferencePage extends FieldEditorPreferencePage {
	public MFieldEditorPreferencePage() {
		super(GRID);
		noDefaultAndApplyButton();
	}

	@Override
	public void performDefaults() {
		IPreferenceStore store = getPreferenceStore();
		if (store instanceof PrefixedPreferenceStore) {
			String[] preferenceNames = ((PrefixedPreferenceStore) store).preferenceNames();
			for (String id : preferenceNames) {
				store.setToDefault(id);
			}
		}
		super.performDefaults();
	}
}
