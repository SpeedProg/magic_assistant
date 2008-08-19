package com.reflexit.magiccards.ui.wizards;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.core.model.nav.DecksContainer;
import com.reflexit.magiccards.core.model.nav.ModelRoot;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (deck).
 */
public class NewDeckContainerWizardPage extends NewCardElementWizardPage {
	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public NewDeckContainerWizardPage(ISelection selection) {
		super(selection);
		setTitle("Create a new deck container");
		setDescription("This wizard creates a new deck container with a given name and place it in specified parent deck container.");
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.wizards.NewCardElementWizardPage#updateInitialSelection()
	 */
	@Override
	protected void updateInitialSelection() {
		super.updateInitialSelection();
		Object firstElement = ((IStructuredSelection) this.selection).getFirstElement();
		if (!(firstElement instanceof DecksContainer)) {
			this.selection = new StructuredSelection(getRootContainer());
		}
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.wizards.NewCardElementWizardPage#dialogChanged()
	 */
	@Override
	protected void dialogChanged() {
		super.dialogChanged();
		if (getErrorMessage() != null)
			return;
		ModelRoot root = DataManager.getModelRoot();
		String containerName = getContainerName();
		CardElement parent = root.findElement(new Path(containerName));
		if (!(parent instanceof DecksContainer)) {
			updateStatus("Parent folder is not a deck container");
			return;
		}
		String name = getElementName();
		if (((DecksContainer) parent).findElement(new Path(name)) != null) {
			updateStatus("Container with this name already exists");
			return;
		}
	}

	@Override
	public String getElementTypeName() {
		return "deck container";
	}

	@Override
	public String getElementCapitalTypeName() {
		return "Deck Container";
	}

	@Override
	protected CardOrganizer getRootContainer() {
		DecksContainer root = DataManager.getModelRoot().getDeckContainer();
		return root;
	}
}