package com.reflexit.magiccards.ui.jobs;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.reflexit.magiccards.core.exports.IExportDelegate;
import com.reflexit.magiccards.core.exports.ReportType;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.ui.MagicUIActivator;
import com.reflexit.magiccards.ui.utils.CoreMonitorAdapter;

public class ExportDeckJob extends Job {
	private ReportType reportType;
	private boolean header;
	private IFilteredCardStore<IMagicCard> filteredLibrary;
	private OutputStream outStream;
	private ICardField[] columns;

	public ExportDeckJob(OutputStream outStream, ReportType reportType, boolean header,
			IFilteredCardStore<IMagicCard> filteredLibrary) {
		super("Exporting " + reportType.getLabel());
		this.reportType = reportType;
		this.header = header;
		this.filteredLibrary = filteredLibrary;
		this.outStream = outStream;
	}

	public ExportDeckJob(OutputStream outStream2, ReportType reportType2, boolean header2,
			IFilteredCardStore filteredLibrary2,
			ICardField[] columns) {
		this(outStream2, reportType2, header2, filteredLibrary2);
		this.columns = columns;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		final IExportDelegate<IMagicCard> worker;
		try {
			worker = reportType.getExportDelegate();
			if (worker == null)
				return new Status(IStatus.ERROR, MagicUIActivator.PLUGIN_ID, "No exporter defined for "
						+ reportType.getLabel());
			worker.setColumns(columns == null ? MagicCardField.allNonTransientFields(true) : columns);
		} catch (Exception e) {
			return new Status(IStatus.ERROR, MagicUIActivator.PLUGIN_ID, e.getMessage(), e);
		}
		worker.init(outStream, header, filteredLibrary);
		try {
			worker.run(new CoreMonitorAdapter(monitor));
			outStream.flush();
		} catch (InvocationTargetException in) {
			Throwable e = in.getCause();
			return new Status(IStatus.ERROR, MagicUIActivator.PLUGIN_ID, e.getMessage(), e);
		} catch (InterruptedException e) {
			return Status.CANCEL_STATUS;
		} catch (IOException e) {
			return new Status(IStatus.ERROR, MagicUIActivator.PLUGIN_ID, e.getMessage(), e);
		}
		return Status.OK_STATUS;
	}

	public void syncRun() throws InterruptedException, InvocationTargetException {
		schedule();
		join();
		IStatus res = getResult();
		if (res.isOK())
			return;
		if (res.getSeverity() == IStatus.CANCEL)
			throw new InterruptedException();
		throw new InvocationTargetException(res.getException());
	}
}
