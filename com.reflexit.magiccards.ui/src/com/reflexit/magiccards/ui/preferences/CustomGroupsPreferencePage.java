package com.reflexit.magiccards.ui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.reflexit.magiccards.core.model.GroupOrder;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dialogs.MagicGroupsSelectoryDialog;
import com.reflexit.magiccards.ui.preferences.feditors.ListEditor2;
import com.reflexit.magiccards.ui.preferences.feditors.StringListFieldEditor;

public class CustomGroupsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public static final String PREF_NAME = "custom.groups";
	private ListEditor2 listEditor;

	public CustomGroupsPreferencePage() {
		super(GRID);
		setPreferenceStore(PreferenceInitializer.getGlobalStore());
	}

	public List<GroupOrder> getCurrentValue() {
		ArrayList<GroupOrder> res = new ArrayList<>();
		String value = getPreferenceStore().getString(PREF_NAME);
		if (value == null || value.isEmpty())
			return res;
		String ids[] = value.split(",");
		for (String string : ids) {
			if (string.isEmpty())
				continue;
			res.add(new GroupOrder(string));
		}
		return res;
	}

	@Override
	protected void createFieldEditors() {
		final Composite parent = getFieldEditorParent();
		listEditor = new StringListFieldEditor(PREF_NAME, "Custom Groups", parent) {
			@Override
			protected String getNewInputObject() {
				String newType = createNewType();
				return newType;
			}

			@Override
			protected String editElement(String string) {
				return editType(string);
			}

			@Override
			protected void selectionChanged() {
				super.selectionChanged();
				String[] selection = list.getSelection();
				getEditButton().setEnabled(selection.length == 1);
				getRemoveButton().setEnabled(selection.length > 0);
			}
		};
		addField(listEditor);
	}

	protected String editType(String string) {
		MagicGroupsSelectoryDialog dialog = new MagicGroupsSelectoryDialog(getShell(), string);
		if (dialog.open() == Window.OK) {
			return dialog.getValue();
		}
		return null;
	}

	protected String createNewType() {
		MagicGroupsSelectoryDialog dialog = new MagicGroupsSelectoryDialog(getShell(), null);
		if (dialog.open() == Window.OK) {
			return dialog.getValue();
		}
		return null;
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(MagicUIActivator.getDefault().getPreferenceStore());
	}

	@Override
	public boolean performOk() {
		MessageDialog.openInformation(getShell(), "Warning",
				"You have to restart the application for this preference to take effect");
		return super.performOk();
	}
}
