package com.reflexit.magiccards.ui.wizards;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.core.model.nav.CollectionsContainer;
import com.reflexit.magiccards.core.model.nav.ModelRoot;

/**
 * The "New" wizard page allows setting the container for the new file as well as the file name. The
 * page will only accept file name without the extension OR with the extension that matches the
 * expected one (deck).
 */
public class NewCardCollectionWizardPage extends NewCardElementWizardPage {
	/**
	 * Constructor for SampleNewWizardPage.
	 *
	 * @param pageName
	 */
	public NewCardCollectionWizardPage(ISelection selection) {
		super(selection);
		setTitle("Create a new " + getElementTypeName());
		setDescription("This wizard creates a new " + getElementTypeName() +
				" with a given name and place it in specified parent container.");
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
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
		String id = parent.getPath().append(name).getId();
		if (root.findCardCollectionById(id) != null) {
			updateStatus("Collection or Deck with this name already exists");
			return;
		}
	}

	@Override
	public String getElementTypeName() {
		return "card collection";
	}

	@Override
	protected void createOptionsGroup(Composite container) {
		virtual = new Button(container, SWT.CHECK);
		virtual.setText("This " + getElementTypeName()
				+ " is virtual (affect card ownership attribute and cards operations)");
		virtual.setToolTipText("Virtual flag affect move/copy/inreaste/descrease operations on a collection. Also it automatically set flags to own for non-virtual collection.");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = ((GridLayout) container.getLayout()).numColumns;
		virtual.setLayoutData(gd);
	}

	@Override
	protected CardOrganizer getRootContainer() {
		return getModelRoot().getCollectionsContainer();
	}
}