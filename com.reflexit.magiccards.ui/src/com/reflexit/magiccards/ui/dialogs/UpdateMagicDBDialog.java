package com.reflexit.magiccards.ui.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.MagicGathererPreferencePage;

public class UpdateMagicDBDialog extends TitleAreaDialog implements IPreferencePageContainer {
	private MagicGathererPreferencePage updatePreference;

	public UpdateMagicDBDialog(Shell parentShell) {
		super(parentShell);
		// getShell();
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Update Magic Database...");
		setTitle("Update MTG Database cards");
		setMessage("Select a set from drop down list or type in the field and press \"Update\" button to update");
		this.updatePreference = new MagicGathererPreferencePage();
		this.updatePreference.setContainer(this);
		this.updatePreference.init(PlatformUI.getWorkbench());
		this.updatePreference.noDefaultAndApplyButton();
		this.updatePreference.noUpdateButton();
		this.updatePreference.createControl(parent);
		this.updatePreference.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		// Build the separator line
		Label titleBarSeparator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
		titleBarSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return this.updatePreference.getControl();
	}

	@Override
	public IPreferenceStore getPreferenceStore() {
		return MagicUIActivator.getDefault().getPreferenceStore();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getOKButton().setText("Update");
		getCancelButton().setText("Close");
	}

	@Override
	protected void okPressed() {
		this.updatePreference.performUpdate();
		super.okPressed();
	}

	@Override
	public void updateButtons() {
	}

	@Override
	public void updateMessage() {
		String message = null;
		String errorMessage = null;
		if (this.updatePreference != null) {
			message = this.updatePreference.getMessage();
			errorMessage = this.updatePreference.getErrorMessage();
		}
		setMessage(message);
		setErrorMessage(errorMessage);
	}

	@Override
	public void updateTitle() {
		if (this.updatePreference != null)
			setTitle(this.updatePreference.getTitle());
	}
}
