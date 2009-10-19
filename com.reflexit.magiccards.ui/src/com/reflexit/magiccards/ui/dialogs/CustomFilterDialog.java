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

import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.CardFilterPreferencePage;

public class CustomFilterDialog extends TitleAreaDialog implements IPreferencePageContainer {
	private CardFilterPreferencePage cardFilterPreference;

	public CustomFilterDialog(Shell parentShell) {
		super(parentShell);
		// getShell();
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText("Filter Cards...");
		this.cardFilterPreference = new CardFilterPreferencePage();
		this.cardFilterPreference.setContainer(this);
		this.cardFilterPreference.noDefaultAndApplyButton();
		this.cardFilterPreference.createControl(parent);
		this.cardFilterPreference.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		// Build the separator line
		Label titleBarSeparator = new Label(parent, SWT.HORIZONTAL | SWT.SEPARATOR);
		titleBarSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return this.cardFilterPreference.getControl();
	}

	public IPreferenceStore getPreferenceStore() {
		return MagicUIActivator.getDefault().getPreferenceStore();
	}

	@Override
	protected void okPressed() {
		this.cardFilterPreference.performOk();
		super.okPressed();
	}

	public void updateButtons() {
		// TODO Auto-generated method stub
	}

	public void updateMessage() {
		String message = null;
		String errorMessage = null;
		if (this.cardFilterPreference != null) {
			message = this.cardFilterPreference.getMessage();
			errorMessage = this.cardFilterPreference.getErrorMessage();
		}
		setMessage(message);
		setErrorMessage(errorMessage);
	}

	public void updateTitle() {
		if (this.cardFilterPreference != null)
			setTitle(this.cardFilterPreference.getTitle());
	}
}
