package com.reflexit.magicassistant.p2;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.equinox.p2.operations.RepositoryTracker;
import org.eclipse.equinox.p2.operations.UpdateOperation;
import org.eclipse.equinox.p2.ui.LoadMetadataRepositoryJob;
import org.eclipse.jface.dialogs.MessageDialog;

public class UpdateHandlerP2 extends PreloadingRepositoryHandler {
	boolean hasNoRepos = false;
	UpdateOperation operation;

	@Override
	protected void doExecute(LoadMetadataRepositoryJob job) {
		if (hasNoRepos) {
			if (getProvisioningUI().getPolicy().getRepositoriesVisible()) {
				boolean goToSites = MessageDialog.openQuestion(getShell(), Messages.UpdateHandler_NoSitesTitle,
						Messages.UpdateHandler_NoSitesMessage);
				if (goToSites) {
					openManipulateRepositories();
				}
			}
		}
		if (job != null) {
			// Report any missing repositories.
			job.reportAccumulatedStatus();
			if (getProvisioningUI().getPolicy().continueWorkingWithOperation(operation, getShell())) {
				getProvisioningUI().openUpdateWizard(false, operation, job);
			}
		}
	}

	public void openManipulateRepositories() {
		getProvisioningUI().manipulateRepositories(getShell());
	}

	@Override
	protected void doPostLoadBackgroundWork(IProgressMonitor monitor) throws OperationCanceledException {
		operation = getProvisioningUI().getUpdateOperation(null, null);
		// check for updates
		IStatus resolveStatus = operation.resolveModal(monitor);
		if (resolveStatus.getSeverity() == IStatus.CANCEL)
			throw new OperationCanceledException();
	}

	@Override
	protected boolean preloadRepositories() {
		hasNoRepos = false;
		RepositoryTracker repoMan = getProvisioningUI().getRepositoryTracker();
		if (repoMan.getKnownRepositories(getProvisioningUI().getSession()).length == 0) {
			hasNoRepos = true;
			return false;
		}
		return super.preloadRepositories();
	}

	@Override
	protected String getProgressTaskName() {
		return Messages.UpdateHandler_ProgressTaskName;
	}
}
