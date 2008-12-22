package com.reflexit.magiccards.core.exports;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;

/**
 * Super class for CSV exporting
 */
public class CsvExporter {
	protected PrintStream exportStream;
	final static String SEP = ",";

	public CsvExporter(final OutputStream exportStream) {
		this.exportStream = new PrintStream(exportStream);
	}

	public void printLine(final Collection<?> list) {
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			Object element = iter.next();
			exportStream.print(excelEscape(element));
			if (iter.hasNext())
				exportStream.print(SEP);
		}
		exportStream.println();
	}

	/**
	 * @param els
	 */
	public void printLine(Object[] els) {
		for (int i = 0; i < els.length; i++) {
			Object element = els[i];
			exportStream.print(excelEscape(element));
			if (i + 1 < els.length)
				exportStream.print(SEP);
		}
		exportStream.println();
	}

	public static String excelEscape(Object el) {
		if (el == null)
			return "null";
		String str = el.toString();
		// fields containing " must be in quotes and all " changed to ""
		if (str.indexOf('"') >= 0) {
			return "\"" + str.replaceAll("\"", "\"\"") + "\"";
		}
		// fields containing carriage return must be surrounded by double quotes
		if (str.indexOf('\n') >= 0)
			return "\"" + str + "\"";
		// fields that contain , must be surrounded by double quotes
		if (str.indexOf(',') >= 0)
			return "\"" + str + "\"";
		// fields starts or ends with spaces must be in double quotes
		if (str.startsWith(" ") || str.endsWith(" "))
			return "\"" + str + "\"";
		return str;
	}

	public void close() {
		exportStream.close();
	}
}
