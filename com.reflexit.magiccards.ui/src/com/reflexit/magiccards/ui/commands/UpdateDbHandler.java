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

import java.util.Properties;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.ICardHandler;
import com.reflexit.magiccards.core.sync.UpdateCardsFromWeb;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.preferences.MagicGathererPreferencePage;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;
import com.reflexit.magiccards.ui.utils.CoreMonitorAdapter;
import com.reflexit.magiccards.ui.views.MagicDbView;

/**
 * @author Alena
 * 
 */
public class UpdateDbHandler extends AbstractHandler {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands. ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		performUpdate(event);
		return null;
	}

	public void performUpdate(final ExecutionEvent event) {
		final Shell shell = MagicUIActivator.getShell();
		String u1 = event.getParameter(PreferenceConstants.GATHERER_UPDATE_SET);
		if (u1 == null) {
			u1 = MagicGathererPreferencePage.STANDARD;
		}
		final String updateLand = event.getParameter(PreferenceConstants.GATHERER_UPDATE_LAND);
		final String updatePrintings = event.getParameter(PreferenceConstants.GATHERER_UPDATE_PRINT);
		final String set = u1;
		if (u1.equalsIgnoreCase(MagicGathererPreferencePage.ALL)) {
			boolean confirm = MessageDialog.openConfirm(shell, "Warning", "You selected to update All sets. " //
					+ "This operation will take a LONG time. "
					+ "Maybe hours. "
					+ "If you want to update to specific set simply type it in the input field. "
					+ "If you want to update to latest just use 'Standard'. "
					+ "Are you sure you want to update ALL?");
			if (!confirm)
				return;
		}
		Job job = new Job("Updating database...") {
			@Override
			public IStatus run(IProgressMonitor pm) {
				try {
					ICardHandler ch = DataManager.getCardHandler();
					Properties options = new Properties();
					options.put(UpdateCardsFromWeb.UPDATE_BASIC_LAND_PRINTINGS, updateLand);
					options.put(UpdateCardsFromWeb.UPDATE_OTHER_PRINTINGS, updatePrintings);
					options.put(UpdateCardsFromWeb.UPDATE_SPECIAL,
							event.getParameter(PreferenceConstants.GATHERER_UPDATE_SPECIAL));
					if (set.equalsIgnoreCase(MagicGathererPreferencePage.ALL)) {
						options.put(UpdateCardsFromWeb.UPDATE_OTHER_PRINTINGS, "true");
					}
					options.put(UpdateCardsFromWeb.UPDATE_LANGUAGE,
							event.getParameter(PreferenceConstants.GATHERER_UPDATE_LANGUAGE));
					final int rec = ch.downloadUpdates(set, options, new CoreMonitorAdapter(pm));
					shell.getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							try {
								IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
										.getActivePage()
										.findView(MagicDbView.ID);
								if (view != null) {
									((MagicDbView) view).reloadData();
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
							if (rec > 0)
								MessageDialog.openInformation(shell, "Magic Db Update", "Data updated: "
										+ rec + " new records");
							else if (rec == 0)
								MessageDialog.openInformation(shell, "Magic Db Update",
										"No new cards found for selected set");
							else
								MessageDialog
										.openError(shell, "Magic Db Update", "Query returned empty page");
						}
					});
					return Status.OK_STATUS;
				} catch (final InterruptedException e) {
					shell.getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							MessageDialog.openInformation(shell, "Info", "Operation Cancelled");
						}
					});
					return Status.CANCEL_STATUS;
				} catch (final Exception e) {
					MagicUIActivator.log(e);
					shell.getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							MessageDialog.openError(shell, "Error", e.getMessage());
						}
					});
					return Status.OK_STATUS; // we display error ourselves
				} finally {
					pm.done();
				}
			}
		};
		job.setPriority(Job.LONG);
		job.setSystem(false);
		job.setUser(true);
		job.schedule();
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		setBaseEnabled(true);
	}
}
