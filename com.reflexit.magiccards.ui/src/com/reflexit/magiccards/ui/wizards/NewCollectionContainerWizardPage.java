package com.reflexit.magiccards.ui.wizards;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.core.model.nav.CollectionsContainer;
import com.reflexit.magiccards.core.model.nav.ModelRoot;

public class NewCollectionContainerWizardPage extends NewElementWizardPage {
	public NewCollectionContainerWizardPage(ISelection selection) {
		super(selection);
		setTitle("Create a new " + getElementTypeName());
		setDescription("This wizard creates a new " + getElementTypeName()
		        + " with a given name and place it in specified parent deck container.");
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
		return "collection container";
	}

	@Override
	public String getElementCapitalTypeName() {
		return "Collection Container";
	}

	@Override
	protected CardOrganizer getRootContainer() {
		return DataManager.getModelRoot().getCollectionsContainer();
	}
}