package com.reflexit.magiccards.ui.exportWizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

public class DeckImportWizard extends Wizard implements IImportWizard {
	protected DeckImportPage mainPage;
	protected DeckImportPreviewPage previewPage;

	public DeckImportWizard() {
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		addPage(mainPage);
		addPage(previewPage);
	}

	@Override
	public boolean performFinish() {
		mainPage.saveWidgetValues();
		mainPage.performImport(false);
		if (mainPage.getPreviewResult().isOk())
			return true;
		return false;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("Import"); // NON-NLS-1
		setNeedsProgressMonitor(true);
		setForcePreviousAndNextButtons(true);
		mainPage = new DeckImportPage("Import", selection);
		previewPage = new DeckImportPreviewPage("Preview");
	}
}
