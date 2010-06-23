package com.reflexit.magiccards.ui.wizards;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CollectionsContainer;
import com.reflexit.magiccards.core.model.nav.ModelRoot;
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorView;

/**
 * This is a sample new wizard. Its role is to create a new file 
 * resource in the provided container. If the container resource
 * (a folder or a project) is selected in the workspace 
 * when the wizard is opened, it will accept it as the target
 * container. The wizard creates one file with the extension
 * "element". If a sample multi-page editor (also available
 * as a template) is registered for the same extension, it will
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

	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 */
	@Override
	protected void doFinish(String containerName, final String fileName, final boolean virtual, IProgressMonitor monitor)
	        throws CoreException {
		// create a sample file
		monitor.beginTask("Creating " + fileName, 2);
		ModelRoot root = DataManager.getModelRoot();
		final CardElement resource = root.findElement(new Path(containerName));
		if (!(resource instanceof CollectionsContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		monitor.worked(1);
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				CardCollection element = CardsNavigatorView.createNewDeckAction((CollectionsContainer) resource,
				        fileName, page);
				element.setVirtual(virtual);
				setElement(element);
			}
		});
		monitor.worked(1);
	}
}