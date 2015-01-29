package com.reflexit.magiccards.ui.preferences;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.CorePreferenceConstants;
import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.ui.MagicUIActivator;

public class FileLocationsPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {
	public FileLocationsPreferencePage() {
		super(GRID);
		setPreferenceStore(MagicUIActivator.getDefault().getCorePreferenceStore());
		setDescription("Specify locations for data directories, relative path would be appended to current workspace location:\n"
				+ FileUtils.getWorkspace());
	}

	@Override
	public void init(IWorkbench workbench) {
		// nothing
	}

	static class LocationFieldEditor extends DirectoryFieldEditor {
		public LocationFieldEditor(String name, String labelText, Composite parent) {
			super(name, labelText, parent);
		}

		@Override
		protected boolean doCheckState() {
			setErrorMessage(null);
			String fileName = getTextControl().getText();
			fileName = fileName.trim();
			if (fileName.length() == 0 && isEmptyStringAllowed()) {
				return true;
			}
			File file = new File(fileName);
			if (!file.isDirectory()) {
				if (file.isAbsolute()) {
					if (!file.getParentFile().isDirectory())
						setErrorMessage("Parent directory does not exists " + file.getParentFile());
				} else {
					File workspaceFile = FileUtils.getWorkspaceFile(fileName);
					if (!workspaceFile.getParentFile().isDirectory())
						setErrorMessage("Parent direcotory does not exists in workspace " + workspaceFile);
				}
			}
			return getErrorMessage() == null;
		}

		@Override
		protected String changePressed() {
			File f = getAbsoluteFile();
			f = f.getParentFile();
			if (!f.exists())
				f = null;
			File dir = getDirectory(f);
			if (dir == null) {
				return null;
			}
			dir = dir.getAbsoluteFile();
			File workspaceFile = FileUtils.getWorkspace();
			if (dir.equals(workspaceFile))
				return FileUtils.getMagicCardsDir().toString();
			String relative = dir.getName();
			dir = dir.getParentFile();
			while (!dir.equals(workspaceFile)) {
				relative = dir.getName() + "/" + relative;
				dir = dir.getParentFile();
			}
			if (dir.equals(workspaceFile))
				return relative;
			return dir.toString();
		}

		protected File getAbsoluteFile() {
			File f = new File(getTextControl().getText());
			if (!f.isAbsolute()) {
				f = FileUtils.getWorkspaceFile(f.toString());
			}
			return f;
		}

		private File getDirectory(File startingDirectory) {
			DirectoryDialog fileDialog = new DirectoryDialog(getShell(), SWT.OPEN | SWT.SHEET);
			if (startingDirectory != null) {
				fileDialog.setFilterPath(startingDirectory.getPath());
			}
			String dir = fileDialog.open();
			if (dir != null) {
				dir = dir.trim();
				if (dir.length() > 0) {
					return new File(dir);
				}
			}
			return null;
		}
	}

	@Override
	protected void createFieldEditors() {
		// addField(new LocationFieldEditor("workspace.dir", "Settings (workspace):", getFieldEditorParent()));
		addField(new LocationFieldEditor(PreferenceConstants.DIR_BACKUP, "Backup:", getFieldEditorParent()));
		//		addField(new LocationFieldEditor(PreferenceConstants.DIR_MAGICCARDS, "Database (sync):",
		//				getFieldEditorParent()));
	}

	protected File getAbsoluteFile(String path) {
		File f = new File(path);
		if (!f.isAbsolute()) {
			f = FileUtils.getWorkspaceFile(path);
		}
		return f;
	}

	@Override
	public boolean performOk() {
		String old = MagicUIActivator.getDefault().getCorePreferenceStore()
				.getString(CorePreferenceConstants.DIR_MAGICCARDS);
		boolean res = super.performOk();
		String nvalue = MagicUIActivator.getDefault().getCorePreferenceStore()
				.getString(CorePreferenceConstants.DIR_MAGICCARDS);
		if (!old.equals(nvalue)) {
			File dir = getAbsoluteFile(nvalue);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			String x[] = dir.list();
			if (x == null) {
				MessageDialog.openError(getShell(), "Error",
						"New location is not a directory and cannot be created");
				return false;
			}
			boolean yes = false;
			if (x.length == 0) {
				yes = MessageDialog
						.openConfirm(
								getShell(),
								"Import",
								"New location does not contain cards database. "
										+ "Would you like to import existing database into new location automatically?"
						);
				if (yes) {
					try {
						FileUtils.copyTree(getAbsoluteFile(old), dir);
					} catch (IOException e) {
						MessageDialog.openError(getShell(), "Error",
								"Failed to copy database: " + e.getMessage());
						return false;
					}
					yes = MessageDialog.openConfirm(getShell(),
							"Restart",
							"Changing database location requires restart. "
									+ "Restart now?");
				} else {
					yes = MessageDialog.openConfirm(getShell(),
							"Restart",
							"Changing database location requires restart. "
									+ "New database will be instantiated in new location, if it is empty. "
									+ "Current collections has to be imported from old location. Restart?");
				}
			} else {
				yes = MessageDialog.openConfirm(getShell(),
						"Restart",
						"Changing database location requires restart. "
								+ "Restart now?");
			}
			if (yes == false)
				return false;
			else {
				// restart
				PlatformUI.getWorkbench().restart();
			}
		}
		return res;
	}
}
