package com.reflexit.magiccards.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import java.util.Iterator;

import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.views.columns.ColumnCollection;
import com.reflexit.magiccards.ui.views.columns.ColumnManager;
import com.reflexit.magiccards.ui.views.lib.LibView;

public class LibViewPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public LibViewPreferencePage() {
		super(GRID);
		setPreferenceStore(MagicUIActivator.getDefault().getPreferenceStore());
		setDescription("CollectionsContainer View Preferences");
	}

	@Override
	protected void createFieldEditors() {
		ColumnCollection columnCollection = new ColumnCollection(LibView.ID);
		columnCollection.createColumnLabelProviders();
		int i = 0;
		String[] columnNames = new String[columnCollection.getColumnsNumber()];
		for (Iterator iterator = columnCollection.getColumns().iterator(); iterator.hasNext();) {
			ColumnManager col = (ColumnManager) iterator.next();
			columnNames[i++] = col.getColumnFullName();
		}
		addField(new CheckedListEditor(PreferenceConstants.LIBVIEW_COLS, "Visible Columns and Order",
		        getFieldEditorParent(), columnNames));
	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
	}
}
