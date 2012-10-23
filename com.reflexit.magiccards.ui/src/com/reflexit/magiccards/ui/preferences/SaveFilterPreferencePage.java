package com.reflexit.magiccards.ui.preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.reflexit.magiccards.ui.dialogs.CardFilterDialog;
import com.reflexit.magiccards.ui.preferences.feditors.LoadFilterPreferenceGroup;

public class SaveFilterPreferencePage extends AbstractFilterPreferencePage {
	private Composite panel;

	public SaveFilterPreferencePage(CardFilterDialog dialog) {
		super(dialog);
		setTitle("Save/Load Filter");
		setDescription("To Save current filter use 'Save As...'. \nTo load existing filter select a filter and press 'Load'.");
	}

	@Override
	protected Control createContents(Composite parent) {
		this.panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		this.panel.setLayout(layout);
		this.panel.setFont(parent.getFont());
		createAndAdd(new LoadFilterPreferenceGroup(dialog), panel);
		return this.panel;
	}
}
