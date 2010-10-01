package com.reflexit.magiccards.core.exports;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public abstract class AbstractExportDelegate<T> implements ICoreRunnableWithProgress, IExportDelegate<T> {
	protected boolean header;
	protected IFilteredCardStore<T> store;
	protected OutputStream st;

	public void init(OutputStream st, boolean header, IFilteredCardStore<T> filteredLibrary) {
		this.st = st;
		this.header = header;
		this.store = filteredLibrary;
	}

	public void exportToTable(IProgressMonitor monitor, IFilteredCardStore<IMagicCard> store,
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

	protected boolean isForExport(ICardField field) {
		return !field.isTransient();
	}
}
