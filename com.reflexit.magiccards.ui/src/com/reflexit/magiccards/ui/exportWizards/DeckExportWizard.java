package com.reflexit.magiccards.ui.exportWizards;

import java.io.File;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import com.reflexit.magiccards.core.model.abs.ICardField;

public class DeckExportWizard extends Wizard implements IExportWizard {
	private DeckExportPage mainPage;

	public DeckExportWizard() {
	}

	@Override
	public void addPages() {
		addPage(mainPage);
	}

	@Override
	public boolean performFinish() {
		mainPage.saveWidgetValues();
		boolean save = mainPage.saveFile();
		if (save)
			return true;
		return false;
	}

	public File getFile() {
		final String fileName = mainPage.getFileName();
		return new File(fileName);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("Export"); // NON-NLS-1
		setNeedsProgressMonitor(true);
		setForcePreviousAndNextButtons(true);
		mainPage = new DeckExportPage("Export", selection);
	}

	public void setColumns(ICardField[] columns2) {
		mainPage.setColumns(columns2);
	}
}
