package com.reflexit.magiccards.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import java.util.Iterator;

import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.views.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.ColumnManager;

public class MagicDbViewPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public MagicDbViewPreferencePage() {
		super(GRID);
		setPreferenceStore(MagicUIActivator.getDefault().getPreferenceStore());
		setDescription("Magic Db View Preferences");
	}

	protected void createFieldEditors() {
		ColumnCollection columnCollection = new ColumnCollection();
		columnCollection.createColumnLabelProviders();
		int i = 0;
		String[] columnNames = new String[columnCollection.getColumnsNumber()];
		for (Iterator iterator = columnCollection.getColumns().iterator(); iterator.hasNext();) {
			ColumnManager col = (ColumnManager) iterator.next();
			columnNames[i++] = col.getColumnFullName();
		}
		addField(new CheckedListEditor(PreferenceConstants.DVIEW_COLS, "Visible Columns and Order",
		        getFieldEditorParent(), columnNames));
	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
	}
}
