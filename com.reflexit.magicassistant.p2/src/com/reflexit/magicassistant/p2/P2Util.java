package com.reflexit.magicassistant.p2;

import java.net.URI;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.operations.ProvisioningJob;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.operations.UpdateOperation;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;

public class P2Util {
	// XXX Check for updates to this application and return a status.
	public static IStatus checkForUpdates(IProvisioningAgent agent, IProgressMonitor monitor, boolean apply)
			throws OperationCanceledException {
		// IProvisioningAgent agent = (IProvisioningAgent)
		// ServiceHelper.getService(getContext(),
		// IProvisioningAgent.SERVICE_NAME);
		ProvisioningSession session = new ProvisioningSession(agent);
		// the default update operation looks for updates to the currently
		// running profile, using the default profile root marker. To change
		// which installable units are being updated, use the more detailed
		// constructors.
		loadRepository(agent);
		UpdateOperation operation = new UpdateOperation(session);
		SubMonitor sub = SubMonitor.convert(monitor, "Checking for application updates...", 200);
		IStatus status = operation.resolveModal(sub.newChild(100));
		Activator.getDefault().getLog().log(status);
		if (status.getCode() == UpdateOperation.STATUS_NOTHING_TO_UPDATE) {
			Activator.getDefault().getLog().log(new Status(0, Activator.PLUGIN_ID, "Nothing to update"));
			return status;
		}
		if (status.getSeverity() == IStatus.CANCEL)
			throw new OperationCanceledException();
		if (status.getSeverity() != IStatus.ERROR) {
			if (apply) {
				// More complex status handling might include showing the user
				// what
				// updates
				// are available if there are multiples, differentiating patches
				// vs.
				// updates, etc.
				// In this example, we simply update as suggested by the
				// operation.
				ProvisioningJob job = operation.getProvisioningJob(null);
				if (job == null)
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot get updates");
				status = job.runModal(sub.newChild(100));
				if (status.getSeverity() == IStatus.CANCEL)
					throw new OperationCanceledException();
			}
		}
		return status;
	}

	public static void loadRepository(IProvisioningAgent agent) {
		try {
			String url = System.getProperty("ma.repo");
			if (url == null)
				url = "http://mtgbrowser.sourceforge.net/update/1.4/";
			URI repoLocation = new URI(url);
			// Load repository manager
			IMetadataRepositoryManager metadataManager = (IMetadataRepositoryManager) agent
					.getService(IMetadataRepositoryManager.SERVICE_NAME);
			// Load artifact manager
			IArtifactRepositoryManager artifactManager = (IArtifactRepositoryManager) agent
					.getService(IArtifactRepositoryManager.SERVICE_NAME);
			// Load repo
			metadataManager.loadRepository(repoLocation, null);
			artifactManager.loadRepository(repoLocation, null);
			Activator.getDefault().getLog().log(new Status(1, Activator.PLUGIN_ID, "Adding repository: " + url));
		} catch (Exception pe) {
			Activator.getDefault().getLog().log(new Status(1, Activator.PLUGIN_ID, pe.getMessage()));
		}
	}
}
