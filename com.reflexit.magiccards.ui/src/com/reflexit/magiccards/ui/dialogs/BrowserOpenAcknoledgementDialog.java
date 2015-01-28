package com.reflexit.magiccards.ui.dialogs;

import java.net.URL;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import com.reflexit.magiccards.ui.MagicUIActivator;

public class BrowserOpenAcknoledgementDialog extends MessageDialog {
	private URL url;

	public BrowserOpenAcknoledgementDialog(Shell parentShell, String dialogMessage, URL url) {
		super(parentShell, "Web Browser Acknoledgment", null, dialogMessage, INFORMATION,
				new String[] { IDialogConstants.OK_LABEL }, 0);
		setShellStyle(getShellStyle() | SWT.SHEET);
		this.url = url;
	}

	@Override
	public int open() {
		try {
			openURL(url);
		} catch (PartInitException e) {
			message = "Cannot open browser " + e.getLocalizedMessage();
			MagicUIActivator.log(e);
		}
		return super.open();
	}

	public void openURL(URL url) throws PartInitException {
		IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
		IWebBrowser browser = browserSupport.createBrowser(MagicUIActivator.PLUGIN_ID);
		browser.openURL(url);
	}
}
