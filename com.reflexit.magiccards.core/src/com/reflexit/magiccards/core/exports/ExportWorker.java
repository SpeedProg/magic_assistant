package com.reflexit.magiccards.core.exports;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class ExportWorker implements ICoreRunnableWithProgress {
	File file;
	boolean header;
	IFilteredCardStore<IMagicCard> store;

	public ExportWorker(File file, boolean header, IFilteredCardStore<IMagicCard> filteredLibrary) {
		super();
		this.file = file;
		this.header = header;
		this.store = filteredLibrary;
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		runCsvExport(monitor);
	}

	public void runCsvExport(IProgressMonitor monitor) throws InvocationTargetException {
		CsvExporter exporter = null;
		try {
			exporter = new CsvExporter(new FileOutputStream(file));
			exportToTable(monitor, store, exporter, header);
		} catch (FileNotFoundException e) {
			throw new InvocationTargetException(e);
		} finally {
			if (exporter != null)
				exporter.close();
		}
	}

	public void runTablePipeExport(IProgressMonitor monitor) throws InvocationTargetException {
		TableExporter exporter = null;
		try {
			exporter = new TableExporter(new FileOutputStream(file), "|");
			exportToTable(monitor, store, exporter, header);
		} catch (FileNotFoundException e) {
			throw new InvocationTargetException(e);
		} finally {
			if (exporter != null)
				exporter.close();
		}
	}

	public static void exportToTable(IProgressMonitor monitor, IFilteredCardStore<IMagicCard> store,
	        TableExporter exporter, boolean header) {
		try {
			if (monitor == null)
				monitor = new NullProgressMonitor();
			monitor.beginTask("Exporting...", store.getSize());
			Collection<ICardField> fields = new ArrayList<ICardField>();
			for (IMagicCard magicCard : store) {
				IMagicCard card = magicCard;
				Collection<String> names;
				if (card instanceof MagicCardPhisical) {
					names = ((MagicCardPhisical) card).getHeaderNames();
				} else if (card instanceof MagicCard) {
					names = ((MagicCard) card).getHeaderNames();
				} else
					continue;
				for (String name : names) {
					ICardField field = MagicCardFieldPhysical.fieldByName(name);
					if (!field.isTransient())
						fields.add(field);
				}
				break;
			}
			if (header) {
				exporter.printLine(fields);
			}
			for (IMagicCard magicCard : store) {
				IMagicCard card = magicCard;
				if (card instanceof MagicCardPhisical) {
					MagicCardPhisical mc = (MagicCardPhisical) card;
					ArrayList line = new ArrayList();
					for (ICardField field : fields) {
						line.add(mc.getObjectByField(field));
					}
					exporter.printLine(line);
				}
				monitor.worked(1);
			}
		} finally {
			monitor.done();
		}
	}
}
