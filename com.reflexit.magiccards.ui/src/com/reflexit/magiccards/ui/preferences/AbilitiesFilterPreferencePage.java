package com.reflexit.magiccards.ui.preferences;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.reflexit.magiccards.ui.dialogs.CardFilterDialog;
import com.reflexit.magiccards.ui.preferences.feditors.AdvancedTextSeachFieldsPreferenceGroup;

public class AbilitiesFilterPreferencePage extends AbstractFilterPreferencePage {
	private Composite panel;

	public AbilitiesFilterPreferencePage(CardFilterDialog cardFilterDialog) {
		super(cardFilterDialog);
		this.subPages = new ArrayList();
		setTitle("Abilities Filter");
		setDescription("This filter allows to perform advanced abilities search using card oracle text. "
				+ "To search for two abilities (using and) enter them on the same line, for alternative abilities (or) "
				+ "use different lines, use 'Excluding' line to remove some cards from the search result.");
		// setDescription("A demonstration of a preference page
		// implementation");
	}

	@Override
	protected Control createContents(Composite parent) {
		this.panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		this.panel.setLayout(layout);
		this.panel.setFont(parent.getFont());
		createAndAdd(new AdvancedTextSeachFieldsPreferenceGroup(), panel);
		return this.panel;
	}
}
