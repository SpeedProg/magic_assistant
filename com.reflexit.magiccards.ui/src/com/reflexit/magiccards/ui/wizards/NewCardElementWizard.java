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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.nav.CardElement;

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
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (MagicException e) {
			MessageDialog.openError(getShell(), "Error", e.getMessage());
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
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
	protected abstract void doFinish(String containerName, String fileName, boolean virtual, IProgressMonitor monitor) throws CoreException;

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
}