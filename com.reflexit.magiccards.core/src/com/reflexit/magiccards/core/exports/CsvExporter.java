package com.reflexit.magiccards.core.exports;

import java.io.OutputStream;

/**
 * Super class for CSV exporting
 */
public class CsvExporter extends TableExporter {
	public CsvExporter(final OutputStream exportStream) {
		super(exportStream, ",");
	}

	@Override
	protected String escape(String element) {
		return excelEscape(element);
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
}
