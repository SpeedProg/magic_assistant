package com.reflexit.magiccards.ui.exportWizards;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.IOverwriteQuery;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.exports.IExportDelegate;
import com.reflexit.magiccards.core.exports.ImportExportFactory;
import com.reflexit.magiccards.core.exports.ReportType;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.utils.CoreMonitorAdapter;

public class DeckExportWizard extends Wizard implements IExportWizard {
	private DeckExportPage mainPage;
	private ICardField[] columns;

	public DeckExportWizard() {
	}

	@Override
	public void addPages() {
		addPage(mainPage);
	}

	@Override
	public boolean performFinish() {
		mainPage.saveWidgetValues();
		boolean save = saveFile();
		if (save)
			return true;
		return false;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("Export"); // NON-NLS-1
		setNeedsProgressMonitor(true);
		setForcePreviousAndNextButtons(true);
		mainPage = new DeckExportPage("Export", selection);
	}

	public void setColumns(ICardField[] columns2) {
		this.columns = columns2;
	}

	public boolean saveFile() {
		final String fileName = mainPage.getFileName();
		final ReportType reportType = mainPage.getReportType();
		if (new File(fileName).exists()) {
			String res = mainPage.queryOverwrite(fileName);
			if (res == IOverwriteQuery.CANCEL)
				return false;
			if (res == IOverwriteQuery.NO)
				return false;
		}
		boolean res = false;
		try {
			final IExportDelegate<IMagicCard> worker;
			try {
				worker = new ImportExportFactory<IMagicCard>().getExportWorker(reportType);
				worker.setColumns(columns == null ? MagicCardFieldPhysical.allNonTransientFields() : columns);
			} catch (Exception e) {
				throw new InvocationTargetException(e);
			}
			// TODO: export selection only
			final boolean header = mainPage.getIncludeHeader();
			final MagicCardFilter locationFilter = mainPage.getLocationFilter();
			IRunnableWithProgress work = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					IFilteredCardStore filteredLibrary = DataManager.getCardHandler().getLibraryFilteredStoreWorkingCopy();
					try {
						filteredLibrary.update(locationFilter);
						worker.init(new FileOutputStream(fileName), header, filteredLibrary);
						worker.run(new CoreMonitorAdapter(monitor));
					} catch (FileNotFoundException e) {
						throw new InvocationTargetException(e);
					}
				}
			};
			mainPage.getRunnableContext().run(true, true, work);
			if (reportType == reportType.XML) {
				// TODO: export multiple files? zip?
				try {
					CardElement ce = mainPage.getFirstCardElement();
					FileUtils.copyFile(ce.getFile(), new File(fileName));
				} catch (Exception e) {
					throw new InvocationTargetException(e);
				}
			}
			return true;
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof InterruptedException) {
				mainPage.displayErrorDialog("Export cancelled");
			} else
				mainPage.displayErrorDialog(e.getCause());
		} catch (InterruptedException e) {
			mainPage.displayErrorDialog("Export cancelled");
		}
		return res;
	}
}
