package com.reflexit.magiccards_rcp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.internal.p2.core.helpers.ServiceHelper;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.operations.UpdateOperation;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import com.reflexit.magicassistant.p2.P2Util;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.commands.CheckForUpdateDbHandler;
import com.reflexit.magiccards.ui.commands.UpdateHandler;
import com.reflexit.magiccards.ui.preferences.PreferenceConstants;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {
	private static final String JUSTUPDATED = "justUpdated";

	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	@Override
	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	@Override
	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setInitialSize(new Point(1600, 900));
		configurer.setShowCoolBar(false);
		configurer.setShowStatusLine(true);
		configurer.setShowProgressIndicator(true);
	}

	@Override
	public void postWindowOpen() {
		try {
			installSoftwareUpdate();
			checkForCardUpdates();
		} catch (Throwable e) {
			Activator.log(e);
		}
	}

	private void checkForCardUpdates() {
		final boolean updates = MagicUIActivator.getDefault().getPreferenceStore()
				.getBoolean(PreferenceConstants.CHECK_FOR_CARDS);
		if (updates == false || MagicUIActivator.TRACE_TESTING || MagicUIActivator.isJunitRunning())
			return;
		new Job("Checking for Card Update") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (updates == false || MagicUIActivator.TRACE_TESTING || MagicUIActivator.isJunitRunning())
					return Status.CANCEL_STATUS;
				CheckForUpdateDbHandler.doCheckForCardUpdates();
				return Status.OK_STATUS;
			}
		}.schedule(1000 * 60 * 1);
	}

	protected void installSoftwareUpdate() {
		final boolean updates = MagicUIActivator.getDefault().getPreferenceStore()
				.getBoolean(PreferenceConstants.CHECK_FOR_UPDATES);
		if (updates == false || MagicUIActivator.TRACE_TESTING || MagicUIActivator.isJunitRunning())
			return;
		final IProvisioningAgent agent = (IProvisioningAgent) ServiceHelper
				.getService(Activator.getDefault().getBundle().getBundleContext(), IProvisioningAgent.SERVICE_NAME);
		if (agent == null) {
			Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"No provisioning agent found.  This application is not set up for updates."));
		}
		// XXX if we're restarting after updating, don't check again.
		final IPreferenceStore prefStore = Activator.getDefault().getPreferenceStore();
		if (prefStore.getBoolean(JUSTUPDATED)) {
			prefStore.setValue(JUSTUPDATED, false);
			return;
		}
		new Job("Checking for Software Update") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (updates == false || MagicUIActivator.TRACE_TESTING || MagicUIActivator.isJunitRunning())
					return Status.CANCEL_STATUS;
				monitor.beginTask("Checking for application updates...", 100);
				IStatus updateStatus = P2Util.checkForUpdates(agent, new SubProgressMonitor(monitor, 50), false);
				if (updateStatus.getCode() != UpdateOperation.STATUS_NOTHING_TO_UPDATE) {
					if (updateStatus.getSeverity() != IStatus.ERROR) {
						// update is available
						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
								if (MessageDialog.openQuestion(shell, "Updates",
										"New software update is available, would you like to install it?")) {
									new UpdateHandler().execute(null);
								}
							}
						});
					}
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		}.schedule(1000 * 60 * 5);
	}
}
