package com.reflexit.mtgtournament.ui.preferences;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.reflexit.mtgtournament.core.PreferenceConstants;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */
public class TournamentPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public TournamentPreferencePage() {
		super(GRID);
		// IEclipsePreferences store =
		// DefaultScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		IPreferenceStore store = new ScopedPreferenceStore(ConfigurationScope.INSTANCE, com.reflexit.mtgtournament.core.Activator.PLUGIN_ID);
		setPreferenceStore(store);
		// setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Preferences for Tournament Organizer");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {
		addField(new IntegerFieldEditor(PreferenceConstants.P_WIN, "Points for win:", getFieldEditorParent()));
		addField(new IntegerFieldEditor(PreferenceConstants.P_LOOSE, "Points for loss:", getFieldEditorParent()));
		addField(new IntegerFieldEditor(PreferenceConstants.P_DRAW, "Points for draw:", getFieldEditorParent()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
}