package com.reflexit.magiccards.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.feditors.CheckedListEditor;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.lib.MyCardsView;

public class LibViewPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public LibViewPreferencePage() {
		super(GRID);
		setPreferenceStore(MagicUIActivator.getDefault().getPreferenceStore());
		setDescription("My Cards View Preferences");
	}

	@Override
	protected void createFieldEditors() {
		ColumnCollection columnCollection = new ColumnCollection(MyCardsView.ID);
		columnCollection.createColumnLabelProviders();
		addField(new CheckedListEditor(PreferenceConstants.LIBVIEW_COLS, "Visible Columns and Order",
		        getFieldEditorParent(), columnCollection.getColumnNames()));
	}

	public void init(IWorkbench workbench) {
		// nothing
	}
}
