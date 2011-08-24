package com.reflexit.magiccards.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.feditors.CheckedListEditor;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.MagicColumnCollection;
import com.reflexit.magiccards.ui.views.lib.DeckView;

public class DeckViewPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public DeckViewPreferencePage() {
		super(GRID);
		setPreferenceStore(MagicUIActivator.getDefault().getPreferenceStore());
		setDescription("Deck View Preferences");
	}

	@Override
	protected void createFieldEditors() {
		ColumnCollection columnCollection = new MagicColumnCollection(DeckView.ID);
		columnCollection.createColumnLabelProviders();
		addField(new BooleanFieldEditor(PreferenceConstants.DECKVIEW_SHOW_QUICKFILTER, "Show quick filter", getFieldEditorParent()));
		addField(new CheckedListEditor(PreferenceConstants.DECKVIEW_COLS, "Visible Columns and Order", getFieldEditorParent(),
				columnCollection.getColumnNames()));
	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
	}
}
