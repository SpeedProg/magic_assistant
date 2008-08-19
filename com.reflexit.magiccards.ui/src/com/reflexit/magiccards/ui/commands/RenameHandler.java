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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.Deck;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.views.lib.DeckView;

/**
 * @author Alena
 *
 */
public class RenameHandler extends AbstractHandler {
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
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
		if (f.getParent() == DataManager.getModelRoot()) {
			return null;
		}
		boolean wasOpen = false;
		if (f instanceof Deck) {
			wasOpen = ((Deck) f).isOpen();
		}
		InputDialog inputDialog = new InputDialog(window.getShell(), "Rename", "New Name", f.getName(), null);
		if (inputDialog.open() == Dialog.OK && !f.getName().equals(inputDialog.getValue())) {
			CardElement el = f.rename(inputDialog.getValue());
			if (wasOpen && el instanceof Deck) {
				try {
					window.getActivePage().showView(DeckView.ID, ((Deck) el).getFileName(),
					        IWorkbenchPage.VIEW_ACTIVATE);
				} catch (PartInitException e) {
					MagicUIActivator.log(e);
				}
			}
		}
		//f.rename();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#setEnabled(java.lang.Object)
	 */
	@Override
	public void setEnabled(Object eo) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		ISelection selection = window.getSelectionService().getSelection();
		if (selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
			setBaseEnabled(false);
			return;
		}
		IStructuredSelection iss = (IStructuredSelection) selection;
		if (iss.size() > 1) {
			setBaseEnabled(false);
			return;
		}
		CardElement f = (CardElement) iss.getFirstElement();
		if (f.getParent() == DataManager.getModelRoot()) {
			setBaseEnabled(false);
			return;
		}
		setBaseEnabled(true);
	}
}
