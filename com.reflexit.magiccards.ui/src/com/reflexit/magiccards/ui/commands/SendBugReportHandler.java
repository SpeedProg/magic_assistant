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

import java.io.File;
import java.net.URL;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.handlers.HandlerUtil;

import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.dnd.CopySupport;

/**
 * @author Alena
 * 
 */
public class SendBugReportHandler extends AbstractHandler {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
	 * ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
		try {
			IWebBrowser browser = browserSupport.createBrowser(MagicUIActivator.PLUGIN_ID);
			browser.openURL(new URL("https://sourceforge.net/p/mtgbrowser/bugs/new/"));
			// get object which represents the workspace
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			// get location of workspace (java.io.File)
			File workspaceDirectory = workspace.getRoot().getLocation().toFile();
			File log = new File(workspaceDirectory, ".metadata/.log");
			CopySupport.runCopy(log.getAbsolutePath().toString());
			MessageDialog.openInformation(window.getShell(), "Send Bug",
					"Web Browser is open (or will be soon)."
							+ " Use this form to submit bug or feature. "
							+ "You need to login first, I don't want accept anonymous submissions"
							+ " because it is like talking to the wall if I need any feedback."
							+ "\nIf this is a bug please attach the log file located at " + log
							+ " (path is in clipboard already)");
		} catch (Exception e) {
			MessageDialog
					.openInformation(
							window.getShell(),
							"Send Bug",
							"I tried to open browser with URL for submitting the bug, but it failed :(. You can go to page: http://sourceforge.net/p/mtgbrowser/bugs/ and submit problem report.");
		}
		return null;
	}
}
