package com.reflexit.magiccards.core.exports;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;

/**
 * Export objects in text table-line format
 * @author Alena
 *
 */
public class TableExporter {
	protected PrintStream exportStream;
	protected String sep;

	public TableExporter(final OutputStream exportStream, String separator) {
		super();
		this.exportStream = new PrintStream(exportStream);
		this.sep = separator;
	}

	public void close() {
		exportStream.close();
	}

	public void printLine(final Collection<?> list) {
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			Object element = iter.next();
			exportStream.print(escape(toString(element)));
			if (iter.hasNext())
				exportStream.print(getSeparator());
		}
		exportStream.println();
	}

	public String getSeparator() {
		return sep;
	}

	/**
	 * default escaping -kind of dump - just gets rid of separator in string
	 * @param element
	 * @return
	 */
	protected String escape(String element) {
		if (element.contains(sep)) {
			return element.replaceAll("\\Q" + sep, "?");
		}
		return element;
	}

	/**
	 * @param els
	 */
	public void printLine(Object[] els) {
		for (int i = 0; i < els.length; i++) {
			Object element = els[i];
			exportStream.print(escape(toString(element)));
			if (i + 1 < els.length)
				exportStream.print(getSeparator());
		}
		exportStream.println();
	}

	protected String toString(Object element) {
		if (element == null)
			return "";
		return element.toString();
	}
}