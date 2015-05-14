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
package com.reflexit.magiccards.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.core.model.nav.ModelRoot;

/**
 * @author Alena
 *
 */
public class RenameHandler extends AbstractHandler {
	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands. ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		ISelection selection = window.getSelectionService().getSelection();
		if (selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
			return null;
		}
		IStructuredSelection iss = (IStructuredSelection) selection;
		if (iss.size() > 1)
			return null;
		CardElement f = (CardElement) iss.getFirstElement();
		if (f.getParent() == getModelRoot()) {
			return null;
		}
		ModelRoot root = getModelRoot();
		if (f == root.getDefaultLib()) {
			MessageDialog.openInformation(window.getShell(), "Cannot Rename", "'" + f
					+ "' is a special collection which cannot be renamed or moved. "
					+ "Move the cards from it using multi-select if you need to.");
			return null;
		}
		if (f instanceof CardOrganizer) {
			if (f == root.getDeckContainer() || f == root.getCollectionsContainer()) {
				MessageDialog.openInformation(window.getShell(), "Cannot Rename",
						"This is special container which cannot be renamed");
				return null;
			}
			MessageDialog
					.openInformation(
							window.getShell(),
							"Cannot Rename",
							"Rename of the containers is not implement. "
									+ "To rename the container exit the App, "
									+ "then go to your workspace and rename the directory in the file system, then restart the app.");
			return null;
		}
		Location loc = f.getLocation();
		if (loc.isSideboard()) {
			MessageDialog.openInformation(window.getShell(), "Cannot Rename",
					"Cannot rename sideboard. Rename the parent decl/collection of this sideboard instead.");
			return null;
		}
		InputDialog inputDialog = new InputDialog(window.getShell(), "Rename", "New Name", f.getName(), null);
		if (inputDialog.open() == Dialog.OK) {
			String newName = inputDialog.getValue();
			if (!f.getName().equals(newName)) {
				Location sb = loc.toSideboard();
				CardElement el = f.rename(newName);
				CardElement fsb = f.getParent().findChieldByName(sb.getBaseFileName());
				if (fsb != null) {
					fsb.rename(Location.valueOf(newName).toSideboard().toString());
				}
			}
		}
		// f.rename();
		return null;
	}

	protected ModelRoot getModelRoot() {
		return DataManager.getInstance().getModelRoot();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.commands.AbstractHandler#setEnabled(java.lang.Object)
	 */
	@Override
	public void setEnabled(Object eo) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null)
			return;
		ISelection selection = window.getSelectionService().getSelection();
		if (selection == null || selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
			setBaseEnabled(false);
			return;
		}
		IStructuredSelection iss = (IStructuredSelection) selection;
		if (iss.size() > 1) {
			setBaseEnabled(false);
			return;
		}
		if (iss.getFirstElement() instanceof CardElement) {
			CardElement f = (CardElement) iss.getFirstElement();
			if (f.getParent() == getModelRoot()) {
				setBaseEnabled(false);
				return;
			}
			setBaseEnabled(true);
		}
	}
}
