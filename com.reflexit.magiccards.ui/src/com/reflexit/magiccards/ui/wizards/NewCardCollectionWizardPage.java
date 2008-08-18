package com.reflexit.magiccards.ui.wizards;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.core.model.nav.CollectionsContainer;
import com.reflexit.magiccards.core.model.nav.ModelRoot;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (deck).
 */
public class NewCardCollectionWizardPage extends NewElementWizardPage {
	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public NewCardCollectionWizardPage(ISelection selection) {
		super(selection);
		setTitle("Create a new card collection");
		setDescription("This wizard creates a new card collection with a given name and place it in specified parent deck container.");
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.ui.wizards.NewElementWizardPage#dialogChanged()
	 */
	@Override
	protected void dialogChanged() {
		super.dialogChanged();
		if (getErrorMessage() != null)
			return;
		ModelRoot root = DataManager.getModelRoot();
		String containerName = getContainerName();
		CardElement parent = root.findElement(new Path(containerName));
		if (!(parent instanceof CollectionsContainer)) {
			updateStatus("Parent folder is not a collection container");
			return;
		}
		String name = getElementName();
		if (((CollectionsContainer) parent).findElement(new Path(name + ".xml")) != null) {
			updateStatus("Collection with this name already exists");
			return;
		}
	}

	@Override
	public String getElementTypeName() {
		return "card collection";
	}

	@Override
	public String getElementCapitalTypeName() {
		return "Card Collection";
	}

	@Override
	protected CardOrganizer getRootContainer() {
		return DataManager.getModelRoot().getCollectionsContainer();
	}
}