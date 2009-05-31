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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import java.lang.reflect.InvocationTargetException;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.ICardHandler;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;

/**
 * @author Alena
 *
 */
public class UpdateDbHandler extends AbstractHandler {
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		performUpdate(event);
		return null;
	}

	public void performUpdate(ExecutionEvent event) {
		IWorkbench wb = PlatformUI.getWorkbench();
		final Shell shell = wb != null ? wb.getActiveWorkbenchWindow().getShell() : new Shell();
		String u1 = event.getParameter(PreferenceConstants.GATHERER_UPDATE_SET);
		if (u1 == null) {
			u1 = "Standard";
		}
		final String set = u1;
		final Integer res[] = new Integer[1];
		try {
			IRunnableWithProgress op = new IRunnableWithProgress() {
				public void run(IProgressMonitor pm) {
					try {
						ICardHandler ch = DataManager.getCardHandler();
						int rec = ch.downloadUpdates(set, pm);
						res[0] = new Integer(rec);
					} catch (final InterruptedException e) {
						shell.getDisplay().syncExec(new Runnable() {
							public void run() {
								MessageDialog.openInformation(shell, "Info", "Operation Cancelled");
							}
						});
						res[0] = -2; // cancelled
					} catch (final Exception e) {
						MagicUIActivator.log(e);
						shell.getDisplay().syncExec(new Runnable() {
							public void run() {
								MessageDialog.openError(shell, "Error", e.getMessage());
							}
						});
						res[0] = new Integer(0);
						return;
					} finally {
						pm.done();
					}
				}
			};
			new ProgressMonitorDialog(shell).run(true, true, op);
			//ps.busyCursorWhile(op);
		} catch (InvocationTargetException e) {
			MessageDialog.openError(shell, "Error", e.getMessage());
		} catch (InterruptedException e) {
			// cancel
		}
		int rec = res[0];
		if (rec > 0)
			MessageDialog.openInformation(shell, "Magic Db Update", "Data updated: " + rec + " new records");
		else if (rec == 0)
			MessageDialog.openInformation(shell, "Magic Db Update", "No new cards found for selected set");
		else if (rec == -2) {
			// do nothing
		} else
			MessageDialog.openError(shell, "Magic Db Update", "Query returned empty page");
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		setBaseEnabled(true);
	}
}
