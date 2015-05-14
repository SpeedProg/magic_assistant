package com.reflexit.magiccards.ui.wizards;

import org.eclipse.ui.INewWizard;

import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CollectionsContainer;

/**
 * This is a sample new wizard. Its role is to create a new file resource in the provided container. If the
 * container resource (a folder or
 * a project) is selected in the workspace when the wizard is opened, it will accept it as the target
 * container. The wizard creates one file
 * with the extension "element". If a sample multi-page editor (also available as a template) is registered
 * for the same extension, it will
 * be able to open it.
 */
public class NewDeckWizard extends NewCardElementWizard implements INewWizard {
	public static final String ID = "com.reflexit.magiccards.ui.wizards.NewDeckWizard";

	/**
	 * Constructor for NewDeckWizard.
	 */
	public NewDeckWizard() {
		super();
	}

	/**
	 * Adding the page to the wizard.
	 */
	@Override
	public void addPages() {
		this.page = new NewDeckWizardPage(this.selection);
		addPage(this.page);
	}

	@Override
	protected CardElement doCreateCardElement(CollectionsContainer parent, String name, boolean virtual) {
		CardCollection d = new CardCollection(name + ".xml", parent, true, virtual);
		return d;
	}
}