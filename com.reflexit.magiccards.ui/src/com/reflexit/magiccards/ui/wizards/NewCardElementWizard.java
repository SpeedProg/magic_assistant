/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CollectionsContainer;
import com.reflexit.magiccards.core.model.nav.ModelRoot;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.views.nav.CardsNavigatorView;

/**
 * @author Alena
 *
 */
public abstract class NewCardElementWizard extends Wizard {
	protected NewCardElementWizardPage page;
	protected ISelection selection;
	private CardElement element;

	public CardElement getElement() {
		return element;
	}

	/**
	 * @param element
	 *            the element to set
	 */
	public void setElement(CardElement element) {
		this.element = element;
	}

	/**
	 *
	 */
	public NewCardElementWizard() {
		super();
		setNeedsProgressMonitor(true);
		setWindowTitle("New");
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We will create an
	 * operation and run it using wizard as execution context.
	 */
	@Override
	public boolean performFinish() {
		final String containerName = this.page.getContainerName();
		final String fileName = this.page.getElementName();
		final boolean virtual = page.isVirtual();
		beforeFinish();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerName, fileName, virtual, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(false, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (MagicException e) {
			MessageDialog.openError(getShell(), "Error", e.getMessage());
			MagicUIActivator.log(e);
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			MagicUIActivator.log(e);
			return false;
		}
		return true;
	}

	/**
	 *
	 */
	protected void beforeFinish() {
		// let wizards store values
	}

	/**
	 * @param containerName
	 * @param fileName
	 * @param virtual
	 * @param monitor
	 */
	/**
	 * The worker method. It will find the container, create the file if missing or just replace its contents,
	 * and open the editor on the
	 * newly created file.
	 */
	protected void doFinish(String containerName, final String name, final boolean virtual,
			IProgressMonitor monitor) throws CoreException {
		// create a sample file
		monitor.beginTask("Creating " + name, 2);
		ModelRoot root = getModelRoot();
		final CardElement resource = root.findElement(containerName);
		if (!(resource instanceof CollectionsContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		DataManager.getCardHandler().getLibraryCardStore();// forces to intialize the store
		CollectionsContainer parent = (CollectionsContainer) resource;
		Job job = new Job("Adding a deck/collection " + name) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				final CardElement col = doCreateCardElement(parent, name, virtual);
				setElement(col);
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
								.getActivePage();
						try {
							IViewPart view = page.showView(CardsNavigatorView.ID);
							view.getViewSite().getSelectionProvider()
									.setSelection(new StructuredSelection(col));
						} catch (PartInitException e) {
							// ignore
						}
					}
				});
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException e) {
			// ok
		}
		monitor.done();
	}

	protected abstract CardElement doCreateCardElement(CollectionsContainer parent, String name,
			boolean virtual);

	protected void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, "com.reflexit.magiccards.ui", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if we can initialize from it.
	 *
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

	public ModelRoot getModelRoot() {
		return DataManager.getInstance().getModelRoot();
	}
}