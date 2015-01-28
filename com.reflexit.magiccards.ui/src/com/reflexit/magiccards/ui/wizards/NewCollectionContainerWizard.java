package com.reflexit.magiccards.ui.wizards;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CollectionsContainer;
import com.reflexit.magiccards.core.model.nav.ModelRoot;
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorView;

public class NewCollectionContainerWizard extends NewCardElementWizard implements INewWizard {
	public static final String ID = "com.reflexit.magiccards.ui.wizards.NewCollectionContainerWizard";

	public NewCollectionContainerWizard() {
		super();
	}

	/**
	 * Adding the page to the wizard.
	 */
	@Override
	public void addPages() {
		this.page = new NewCollectionContainerWizardPage(this.selection);
		addPage(this.page);
	}

	/**
	 * The worker method. It will find the container, create the file if missing or just replace its
	 * contents, and open the editor on the newly created file.
	 */
	@Override
	protected void doFinish(String containerName, final String name, boolean virtual, IProgressMonitor monitor)
			throws CoreException {
		// create a sample file
		monitor.beginTask("Creating " + name, 2);
		ModelRoot root = getModelRoot();
		final CardElement resource = root.findElement(containerName);
		if (!(resource instanceof CollectionsContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		monitor.worked(1);
		getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				CollectionsContainer parent = (CollectionsContainer) resource;
				CollectionsContainer con = new CollectionsContainer(name, parent);
				try {
					IViewPart view = page.showView(CardsNavigatorView.ID);
					view.getViewSite().getSelectionProvider().setSelection(new StructuredSelection(con));
				} catch (PartInitException e) {
					// ignore
				}
			}
		});
		monitor.worked(1);
	}

	@Override
	protected CardElement doCreateCardElement(CollectionsContainer parent, String name, boolean virtual) {
		return new CollectionsContainer(name, parent);
	}
}