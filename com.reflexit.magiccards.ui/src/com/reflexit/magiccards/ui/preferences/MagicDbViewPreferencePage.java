package com.reflexit.magiccards.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.reflexit.magiccards.ui.preferences.feditors.CheckedListEditor;
import com.reflexit.magiccards.ui.views.MagicDbView;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;

public class MagicDbViewPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public MagicDbViewPreferencePage() {
		super(GRID);
		setPreferenceStore(PreferenceInitializer.getMdbStore());
		setDescription("MTG Database View Preferences");
	}

	@Override
	protected void createFieldEditors() {
		ColumnCollection columnCollection = new MagicColumnCollection(MagicDbView.ID);
		columnCollection.createColumnLabelProviders();
		addField(new BooleanFieldEditor(PreferenceConstants.LOCAL_SHOW_QUICKFILTER, "Show quick filter", getFieldEditorParent()));
		addField(new CheckedListEditor(PreferenceConstants.LOCAL_COLUMNS, "Visible Columns and Order", getFieldEditorParent(),
				columnCollection.getColumnNames()));
	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
	}
}
