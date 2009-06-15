package com.reflexit.magiccards.ui.wizards;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

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
public class NewCardCollectionWizardPage extends NewCardElementWizardPage {
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

	@Override
	protected void updateInitialSelection() {
		super.updateInitialSelection();
		Object firstElement = ((IStructuredSelection) this.selection).getFirstElement();
		if (!(firstElement instanceof CollectionsContainer)) {
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
		if (!(parent instanceof CollectionsContainer)) {
			updateStatus("Parent folder is not a proper container");
			return;
		}
		String name = getElementName();
		if (root.findCardCollectionById(name + ".xml") != null) {
			updateStatus("Collection or Deck with this name already exists");
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