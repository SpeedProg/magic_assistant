package com.reflexit.magiccards.core.exports;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.monitor.ICoreRunnableWithProgress;

public abstract class AbstractExportDelegate<T> implements ICoreRunnableWithProgress, IExportDelegate<T> {
	protected boolean header;
	protected IFilteredCardStore<T> store;
	protected OutputStream st;
	protected ICardField[] columns;

	public void init(OutputStream st, boolean header, IFilteredCardStore<T> filteredLibrary) {
		this.st = st;
		this.header = header;
		this.store = filteredLibrary;
	}

	public void exportToTable(ICoreProgressMonitor monitor, IFilteredCardStore<IMagicCard> store, TableExporter exporter, boolean header) {
		try {
			if (monitor == null)
				monitor = ICoreProgressMonitor.NONE;
			monitor.beginTask("Exporting...", store.getSize());
			Collection<ICardField> fields = new ArrayList<ICardField>();
			for (IMagicCard magicCard : store) {
				IMagicCard card = magicCard;
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
			if (header) {
				exporter.printLine(fields);
			}
			for (IMagicCard magicCard : store) {
				IMagicCard card = magicCard;
				if (card instanceof MagicCardPhysical) {
					MagicCardPhysical mc = (MagicCardPhysical) card;
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

	public void setColumns(ICardField[] columnsForExport) {
		this.columns = columnsForExport;
	}

	protected boolean isForExport(ICardField field) {
		return !field.isTransient();
	}
}
