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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CardOrganizer;
import com.reflexit.magiccards.core.model.nav.ModelRoot;

/**
 * @author Alena
 * 
 */
public class DeleteHandler extends AbstractHandler {
	/*
	 * (non-Javadoc)
	 * 
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
		remove(f);
		return null;
	}

	public static void remove(CardElement f) {
		ModelRoot root = DataManager.getModelRoot();
		if (f == root.getDefaultLib()) {
			info("'" + f + "' is a special collection which cannot be renamed or moved. "
					+ "Move the cards from it using multi-select if you need to.");
			return;
		}
		if (f instanceof CardOrganizer) {
			if (f == root.getDeckContainer() || f == root.getCollectionsContainer()) {
				info("This is special container which cannot be removed");
				return;
			}
			if (((CardOrganizer) f).hasChildren()) {
				info("Cannot remove container - not empty. Remove all children elements first");
				return;
			}
		}
		f.remove();
	}

	public static void info(final String msg) {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				Shell shell = null;
				try {
					shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				} catch (Exception e) {
					shell = new Shell();
				}
				MessageDialog.openInformation(shell, "Cannot Remove", msg);
			}
		});
	}
}
