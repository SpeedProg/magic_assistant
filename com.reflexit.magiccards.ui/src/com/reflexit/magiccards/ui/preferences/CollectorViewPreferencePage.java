package com.reflexit.magiccards.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.reflexit.magiccards.ui.preferences.feditors.ColumnFieldEditor;
import com.reflexit.magiccards.ui.views.collector.CollectorColumnCollection;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;

public class CollectorViewPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {
	public static final String PPID = CollectorViewPreferencePage.class.getName();

	public CollectorViewPreferencePage() {
		super(GRID);
		setPreferenceStore(PreferenceInitializer.getCollectorStore());
		setDescription("Collector View Preferences");
	}

	@Override
	protected void createFieldEditors() {
		ColumnCollection columnCollection = new CollectorColumnCollection();
		addField(new BooleanFieldEditor(PreferenceConstants.LOCAL_SHOW_QUICKFILTER, "Show quick filter",
				getFieldEditorParent()));
		addField(new ColumnFieldEditor(PreferenceConstants.LOCAL_COLUMNS, "Visible Columns and Order",
				getFieldEditorParent(),
				columnCollection));
	}

	@Override
	public void init(IWorkbench workbench) {
		// nothing
	}
}
