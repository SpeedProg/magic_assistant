package com.reflexit.magiccards.ui.wizards;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.core.model.nav.Deck;
import com.reflexit.magiccards.core.model.nav.DecksContainer;
import com.reflexit.magiccards.core.model.nav.ModelRoot;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (deck).
 */
public class NewDeckWizardPage extends NewElementWizardPage {
	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public NewDeckWizardPage(ISelection selection) {
		super(selection);
		setTitle("Create a new deck");
		setDescription("This wizard creates a new deck with a given name and place it in specified deck container.");
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.wizards.NewElementWizardPage#dialogChanged()
	 */
	@Override
	protected void dialogChanged() {
		super.dialogChanged();
		ModelRoot root = DataManager.getModelRoot();
		String containerName = getContainerName();
		CardElement parent = root.findElement(new Path(containerName));
		if (!(parent instanceof DecksContainer)) {
			updateStatus("Parent folder is not a deck container");
			return;
		}
		String fileName = getElementName();
		Deck old = DataManager.getModelRoot().getDeckContainer().findDeck(fileName + ".xml");
		if (old != null) {
			updateStatus("Deck with this name already exists");
			return;
		}
	}

	@Override
	public String getElementTypeName() {
		return "deck";
	}

	@Override
	public String getElementCapitalTypeName() {
		return "Deck";
	}

	@Override
	protected CardOrganizer getRootContainer() {
		DecksContainer root = DataManager.getModelRoot().getDeckContainer();
		return root;
	}
}