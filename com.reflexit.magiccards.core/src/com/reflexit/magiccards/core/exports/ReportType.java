package com.reflexit.magiccards.core.exports;

import java.util.HashMap;
import java.util.Map;

public class ReportType {
	private String name;
	private String label;
	private static Map<String, ReportType> types = new HashMap<String, ReportType>();
	public static final ReportType XML = new ReportType("xml", "Magic Assistant XML");
	public static final ReportType CSV = new ReportType("csv", "Magic Assistant CSV");
	public static final ReportType TEXT_DECK_CLASSIC = new ReportType("classic", "Deck Classic (Text)");
	public static final ReportType USER_DEFINED = new ReportType("user", "User Defined");
	public static final ReportType TABLE_PIPED = new ReportType("table", "Piped Table");

	private ReportType(String key, String label) {
		this.name = key;
		this.label = label;
		types.put(key, this);
	}

	public static ReportType createReportType(String key, String label) {
		ReportType reportType = types.get(key);
		if (reportType != null)
			return reportType;
		return new ReportType(key, label);
	}

	@Override
	public String toString() {
		return name;
	}

	public String getLabel() {
		return label;
	}

	public static ReportType valueOf(String type) {
		if (type == null)
			return null;
		if (XML.toString().equals(type))
			return XML;
		if (CSV.toString().equals(type))
			return CSV;
		if (TEXT_DECK_CLASSIC.toString().equals(type))
			return TEXT_DECK_CLASSIC;
		if (USER_DEFINED.toString().equals(type))
			return USER_DEFINED;
		if (TABLE_PIPED.toString().equals(type))
			return TABLE_PIPED;
		return null;
	}
}
