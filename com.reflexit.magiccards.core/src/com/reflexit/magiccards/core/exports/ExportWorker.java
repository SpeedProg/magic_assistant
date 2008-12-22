package com.reflexit.magiccards.core.exports;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.ICardStore;

public class ExportWorker implements ICoreRunnableWithProgress {
	File file;
	boolean header;
	ICardStore<IMagicCard> elem;
	CsvExporter exporter;

	public ExportWorker(File file, boolean header, ICardStore<IMagicCard> elem) {
		super();
		this.file = file;
		this.header = header;
		this.elem = elem;
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		try {
			if (monitor == null)
				monitor = new NullProgressMonitor();
			monitor.beginTask("Exporting...", elem.size());
			exporter = new CsvExporter(new FileOutputStream(file));
			if (header) {
				for (IMagicCard magicCard : elem) {
					IMagicCard card = magicCard;
					if (card instanceof MagicCardPhisical)
						exporter.printLine(((MagicCardPhisical) card).getHeaderNames());
					break;
				}
			}
			for (IMagicCard magicCard : elem) {
				IMagicCard card = magicCard;
				if (card instanceof MagicCardPhisical)
					exporter.printLine(((MagicCardPhisical) card).getValues());
				monitor.worked(1);
			}
		} catch (FileNotFoundException e) {
			throw new InvocationTargetException(e);
		} finally {
			monitor.done();
			exporter.close();
		}
	}
}
