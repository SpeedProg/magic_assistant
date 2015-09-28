package com.reflexit.magiccards.core.exports;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ILocatable;
import com.reflexit.magiccards.core.model.storage.MemoryFilteredCardStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public abstract class AbstractExportDelegate<T extends ICard> implements IExportDelegate<T> {
	protected PrintStream stream;
	protected ICardField[] columns;
	protected ReportType type;
	protected boolean header;
	protected IFilteredCardStore<T> store;
	protected Location location;

	@Override
	public void init(OutputStream st, boolean header, IFilteredCardStore<T> filteredLibrary) {
		try {
			this.stream = new PrintStream(st, true, FileUtils.UTF8);
		} catch (UnsupportedEncodingException e) {
			this.stream = new PrintStream(st);
		}
		this.header = header;
		this.store = filteredLibrary;
		this.location = store.getLocation();
	}

	protected String getLabel() {
		ReportType type = getType();
		if (type == null)
			return getClass().getName();
		return type.getLabel();
	}

	public String export(Iterable<T> cards) {
		ByteArrayOutputStream st = new ByteArrayOutputStream();
		MemoryFilteredCardStore<T> mem = new MemoryFilteredCardStore<T>();
		for (T mc : cards) {
			mem.add(mc);
		}
		mem.update();
		init(st, true, mem);
		try {
			run(null);
		} catch (InvocationTargetException e) {
			MagicLogger.log(e);
			return null;
		} catch (InterruptedException e) {
			// ignore
		}
		return st.toString();
	}

	@Override
	public void setReportType(ReportType reportType) {
		this.type = reportType;
	}

	@Override
	public ReportType getType() {
		return type;
	}

	@Override
	public void run(ICoreProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		if (monitor == null)
			monitor = ICoreProgressMonitor.NONE;
		try {
			export(monitor);
		} finally {
			done();
		}
	}

	public void done() {
		if (stream != null)
			stream.close();
	}

	public abstract void export(ICoreProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException;

	@Override
	public void setColumns(ICardField[] columnsForExport) {
		this.columns = columnsForExport;
	}

	public String getName() {
		if (store != null) {
			return ((ILocatable) store).getLocation().getName();
		}
		return "deck";
	}

	@Override
	public boolean isColumnChoiceSupported() {
		return true;
	}

	@Override
	public boolean isMultipleLocationSupported() {
		return true;
	}

	@Override
	public boolean isSideboardSupported() {
		return true;
	}

	public IFilteredCardStore<T> getExampleData() {
		MemoryFilteredCardStore<T> fstore = new MemoryFilteredCardStore<>();
		IImportDelegate del = ImportExportFactory.TABLE_PIPED.getImportDelegate();
		del.init(new ImportData(true, Location.NO_WHERE,
				ImportExportFactory.TABLE_PIPED.getExample()));
		try {
			del.run(ICoreProgressMonitor.NONE);
			fstore.addAll((List) del.getResult().getList());
			fstore.update();
		} catch (InvocationTargetException | InterruptedException e) {
			MagicLogger.log(e);
		}
		return fstore;
	}

	@Override
	public String getExample() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		this.init(out, true, getExampleData());
		try {
			this.run(ICoreProgressMonitor.NONE);
		} catch (InvocationTargetException | InterruptedException e) {
			MagicLogger.log(e);
		}
		return out.toString();
	}
}
