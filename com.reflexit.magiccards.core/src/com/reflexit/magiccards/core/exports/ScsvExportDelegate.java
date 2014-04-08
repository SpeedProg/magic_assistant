package com.reflexit.magiccards.core.exports;

public class ScsvExportDelegate extends CsvExportDelegate {
	@Override
	public String getSeparator() {
		return ";";
	}
}
