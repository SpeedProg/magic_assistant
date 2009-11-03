package com.reflexit.magiccards.ui.preferences.feditors;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Text;

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

	@Override
	public void initialize() {
		super.initialize();
	}

	protected void addTooltip(StringFieldEditor nameSfe, String string) {
		Text textControl = nameSfe.getTextControl(getFieldEditorParent());
		textControl.setToolTipText(string);
	}
}
