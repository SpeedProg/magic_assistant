package com.reflexit.magiccards.ui.preferences.feditors;

import java.util.Collection;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Label;
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
			Collection<String> ids = getIds();
			for (String id : ids) {
				store.setToDefault(id);
			}
		}
		super.performDefaults();
	}

	public abstract Collection<String> getIds();

	@Override
	public void initialize() {
		super.initialize();
	}

	protected void addTooltip(StringFieldEditor nameSfe, String string) {
		Text textControl = nameSfe.getTextControl(getFieldEditorParent());
		textControl.setToolTipText(string);
		Label labelControl = nameSfe.getLabelControl(getFieldEditorParent());
		labelControl.setToolTipText(string);
	}
}
