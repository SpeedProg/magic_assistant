package com.reflexit.magiccards.ui.exportWizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

public class DeckImportWizard extends Wizard implements IImportWizard {
	private DeckImportPage mainPage;
	private DeckImportPreviewPage previewPage;
	private Object data;

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

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
		// TODO Auto-generated method stub
		return super.canFinish();
	}

	@Override
	public boolean performFinish() {
		mainPage.saveWidgetValues();
		boolean ok = mainPage.performImport(false);
		if (ok)
			return true;
		return false;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("Import"); // NON-NLS-1
		setNeedsProgressMonitor(true);
		setForcePreviousAndNextButtons(true);
		mainPage = new DeckImportPage("Import", selection);
		previewPage = new DeckImportPreviewPage("Preview");
	}
}
