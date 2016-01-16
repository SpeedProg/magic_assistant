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
	public boolean canFinish() {
		// Default implementation is to check if all pages are complete.
		for (int i = 0; i < getPageCount(); i++) {
			if (!getPages()[i].isPageComplete()) {
				System.err.println("page " + i);
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean performFinish() {
		mainPage.saveWidgetValues();
		mainPage.performImport(false);
		if (mainPage.getImportData().isOk())
			return true;
		return false;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("Import"); // NON-NLS-1
		setNeedsProgressMonitor(true);
		setForcePreviousAndNextButtons(true);
		mainPage = createMainPage(selection);
		previewPage = new DeckImportPreviewPage("Preview");
	}

	public DeckImportPage createMainPage(IStructuredSelection selection) {
		return new DeckImportPage("Import", selection);
	}
}
