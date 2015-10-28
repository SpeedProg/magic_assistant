package com.reflexit.magiccards.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.reflexit.magiccards.ui.preferences.feditors.ColumnFieldEditor;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;

public class MagicDbViewPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public static final String PPID = MagicDbViewPreferencePage.class.getName();
	public MagicDbViewPreferencePage() {
		super(GRID);
		setPreferenceStore(PreferenceInitializer.getMdbStore());
		setDescription("MTG Database View Preferences");
	}

	@Override
	protected void createFieldEditors() {
		ColumnCollection columnCollection = new MagicColumnCollection(getClass().getName());
		addField(new BooleanFieldEditor(PreferenceConstants.LOCAL_SHOW_QUICKFILTER, "Show quick filter",
				getFieldEditorParent()));
		addField(new ColumnFieldEditor(PreferenceConstants.LOCAL_COLUMNS, "Visible Columns and Order",
				getFieldEditorParent(),
				columnCollection));
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
	}
}
