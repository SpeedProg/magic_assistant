package com.reflexit.magiccards.ui.wizards;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.core.model.nav.CollectionsContainer;
import com.reflexit.magiccards.core.model.nav.ModelRoot;

public class NewCollectionContainerWizardPage extends NewCardElementWizardPage {
	public NewCollectionContainerWizardPage(ISelection selection) {
		super(selection);
		setTitle("Create a new " + getElementTypeName());
		setDescription("This wizard creates a new " + getElementTypeName()
				+ " with a given name and place it in specified parent deck container.");
	}

	@Override
	protected void updateInitialSelection() {
		super.updateInitialSelection();
		Object firstElement = ((IStructuredSelection) this.selection).getFirstElement();
		if (!(firstElement instanceof CollectionsContainer)) {
			this.selection = new StructuredSelection(getRootContainer());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.ui.wizards.NewCardElementWizardPage#dialogChanged()
	 */
	@Override
	protected void dialogChanged() {
		super.dialogChanged();
		if (getErrorMessage() != null)
			return;
		ModelRoot root = getModelRoot();
		String containerName = getContainerName();
		CardElement parent = root.findElement(containerName);
		if (!(parent instanceof CollectionsContainer)) {
			updateStatus("Parent folder is not a proper container");
			return;
		}
		String name = getElementName();
		if (((CollectionsContainer) parent).findElement(name + ".xml") != null) {
			updateStatus("Container with this name already exists");
			return;
		}
	}

	@Override
	public String getElementTypeName() {
		return "collection container";
	}

	@Override
	protected void createOptionsGroup(Composite container) {
	}

	@Override
	protected CardOrganizer getRootContainer() {
		return getModelRoot().getMyCardsContainer();
	}
}