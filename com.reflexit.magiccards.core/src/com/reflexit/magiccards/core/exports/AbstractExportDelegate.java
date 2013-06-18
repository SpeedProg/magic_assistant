package com.reflexit.magiccards.core.exports;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ILocatable;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.monitor.ICoreRunnableWithProgress;

public abstract class AbstractExportDelegate<T> implements ICoreRunnableWithProgress, IExportDelegate<T> {
	protected boolean header;
	protected IFilteredCardStore<T> store;
	protected PrintStream stream;
	protected ICardField[] columns;
	protected Location location;
	protected ReportType type;

	public void init(OutputStream st, boolean header, IFilteredCardStore<T> filteredLibrary) {
		try {
			this.stream = new PrintStream(st, true, FileUtils.UTF8);
		} catch (UnsupportedEncodingException e) {
			this.stream = new PrintStream(st);
		}
		this.header = header;
		this.store = filteredLibrary;
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
		if (getType() == null)
			throw new IllegalArgumentException("Delegate is not initialized");
		export(monitor);
	}

	public void export(ICoreProgressMonitor monitor) {
		try {
			if (monitor == null)
				monitor = ICoreProgressMonitor.NONE;
			monitor.beginTask("Exporting to " + getType().getLabel() + "...", store.getSize());
			if (columns == null) {
				columns = deterimeColumns();
			}
			location = store.getLocation();
			if (location == null)
				location = Location.NO_WHERE;
			printHeader();
			printLocationHeader();
			for (T card : store) {
				Location curLocation = ((ILocatable) card).getLocation();
				if (!location.equals(curLocation)) {
					printLocationFooter();
					location = ((ILocatable) card).getLocation();
					if (location == null)
						location = Location.NO_WHERE;
					printLocationHeader();
				}
				printCard(card);
				monitor.worked(1);
			}
			printLocationFooter();
			printFooter();
		} finally {
			stream.close();
			monitor.done();
		}
	}

	public void printCard(T card) {
		Object[] values = getValues(card);
		printLine(values);
	}

	public Object[] getValues(T card) {
		Object values[] = new Object[columns.length];
		int i = 0;
		for (ICardField field : columns) {
			values[i] = ((ICard) card).getObjectByField(field);
			i++;
		}
		return values;
	}

	public void printLine(Object[] values) {
		for (int i = 0; i < values.length; i++) {
			Object element = values[i];
			stream.print(escape(toString(element)));
			if (i + 1 < values.length)
				stream.print(getSeparator());
		}
		stream.println();
	}

	public String getSeparator() {
		return " ";
	}

	protected String escape(String element) {
		return element;
	}

	protected String toString(Object element) {
		if (element == null)
			return "";
		return element.toString();
	}

	public void printLocationHeader() {
		// nothing
	}

	public void printLocationFooter() {
		// nothing
	}

	public void printHeader() {
		if (header) {
			printLine(columns);
		}
	}

	public void printFooter() {
		// nothing
	}

	public ICardField[] deterimeColumns() {
		Collection<ICardField> fields = new ArrayList<ICardField>();
		for (T card : store) {
			Collection<String> names;
			if (card instanceof MagicCardPhysical) {
				names = ((MagicCardPhysical) card).getHeaderNames();
			} else if (card instanceof MagicCard) {
				names = ((MagicCard) card).getHeaderNames();
			} else
				continue;
			for (String name : names) {
				ICardField field = MagicCardFieldPhysical.fieldByName(name);
				if (isForExport(field))
					fields.add(field);
			}
			break;
		}
		return fields.toArray(new ICardField[fields.size()]);
	}

	public void setColumns(ICardField[] columnsForExport) {
		this.columns = columnsForExport;
	}

	protected boolean isForExport(ICardField field) {
		return !field.isTransient();
	}

	public String getName() {
		if (store != null) {
			return ((ILocatable) store).getLocation().getName();
		}
		return "deck";
	}
}
