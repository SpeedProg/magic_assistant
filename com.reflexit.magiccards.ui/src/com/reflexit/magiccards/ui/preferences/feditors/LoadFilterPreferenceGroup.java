package com.reflexit.magiccards.ui.preferences.feditors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.FilterField;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dialogs.CardFilterDialog;

public class LoadFilterPreferenceGroup extends MFieldEditorPreferencePage {
	private Collection<String> ids = new ArrayList<String>(6);

	@Override
	public Collection<String> getIds() {
		return ids;
	}

	private CardFilterDialog dialog;

	public LoadFilterPreferenceGroup(CardFilterDialog dialog) {
		this.dialog = dialog;
	}

	@Override
	protected void createFieldEditors() {
		String id = MagicUIActivator.PLUGIN_ID + ".last_load_filter";
		getPreferenceStore().setDefault(id, "");
		final PickListEditor listEditor = new PickListEditor(id, "Filters:", getFieldEditorParent()) {
			@Override
			protected String getNewInputObject() {
				String dname = getSelected();
				InputDialog d = new InputDialog(getShell(), "Filter Name", "Save filter as", dname, null);
				if (d.open() == Dialog.OK) {
					String name = d.getValue();
					Collection allIds = FilterField.getAllIds();
					dialog.performOk(); // saved current values
					IPreferenceStore store = getPreferenceStore();
					Properties props = new Properties();
					for (Iterator iterator = allIds.iterator(); iterator.hasNext();) {
						String id = (String) iterator.next();
						String value = store.getString(id);
						props.put(id, value);
					}
					IPath filters = getFilterPath();
					IPath fname = filters.addTrailingSeparator().append(name).addFileExtension("ini");
					try {
						props.store(new FileOutputStream(fname.toFile()), "filter " + name);
					} catch (IOException e) {
						MessageDialog.openError(getShell(), "Error", "Cannot save filter to " + fname);
						return null;
					}
					return name;
				}
				return null;
			}

			@Override
			protected String[] getValues() {
				IPath filters = getFilterPath();
				File[] listFiles = filters.toFile().listFiles();
				ArrayList<String> names = new ArrayList<String>();
				if (listFiles != null) {
					for (File file : listFiles) {
						String name = file.getName().replaceAll("\\.ini$", "");
						names.add(name);
					}
				}
				return names.toArray(new String[names.size()]);
			}

			@Override
			public void loadPressed() {
				String name = getSelected();
				IPath filters = getFilterPath();
				IPath fname = filters.addTrailingSeparator().append(name).addFileExtension("ini");
				Properties prop = new Properties();
				try {
					prop.load(new FileInputStream(fname.toFile()));
				} catch (IOException e) {
					MessageDialog.openError(getShell(), "Error", "Cannot load filter from " + fname);
					return;
				}
				IPreferenceStore store = getPreferenceStore();
				for (Object element : prop.keySet()) {
					String key = (String) element;
					String value = prop.getProperty(key);
					String old = store.getString(key);
					if (value != null && !value.equals(old)) {
						store.putValue(key, value);
						store.firePropertyChangeEvent(key, value, old);
					}
				}
				if (dialog != null) {
					dialog.refresh();
				}
			}

			@Override
			protected void removePressed() {
				String name = getSelected();
				IPath filters = getFilterPath();
				IPath fname = filters.addTrailingSeparator().append(name).addFileExtension("ini");
				fname.toFile().delete();
				super.removePressed();
			}
		};
		addField(listEditor);
	}

	private IPath getFilterPath() {
		File file = new File(FileUtils.getMagicCardsDir(), ".settings/filters");
		file.mkdirs();
		File oldFile = new File(FileUtils.getStateLocationFile(), "filters");
		try {
			FileUtils.migrate(file, oldFile);
		} catch (IOException e) {
			MagicLogger.log(e);
		}
		return new Path(file.toString());
	}
}
