package com.reflexit.magiccards.ui.preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.reflexit.magiccards.ui.dialogs.CardFilterDialog;
import com.reflexit.magiccards.ui.preferences.feditors.UserFieldsPreferenceGroup;

public class UserFilterPreferencePage extends AbstractFilterPreferencePage {
	private Composite panel;

	public UserFilterPreferencePage(CardFilterDialog dialog) {
		super(dialog);
		setTitle("User Filter");
		// setDescription("A demonstration of a preference page
		// implementation");
	}

	@Override
	protected Control createContents(Composite parent) {
		setTitle("User Filter");
		this.panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		this.panel.setLayout(layout);
		this.panel.setFont(parent.getFont());
		createAndAdd(new UserFieldsPreferenceGroup(), panel);
		return this.panel;
	}
}
