package com.reflexit.magiccards.ui.wizards;

import org.eclipse.jface.viewers.ISelection;

import com.reflexit.magiccards.core.model.nav.CardOrganizer;

public class NewDeckWizardPage extends NewCardCollectionWizardPage {
	public NewDeckWizardPage(ISelection selection) {
		super(selection);
	}

	@Override
	public String getElementTypeName() {
		return "deck";
	}

	@Override
	protected CardOrganizer getRootContainer() {
		return getModelRoot().getDeckContainer();
	}
}