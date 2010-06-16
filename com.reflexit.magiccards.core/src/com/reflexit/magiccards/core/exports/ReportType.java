package com.reflexit.magiccards.core.exports;

import java.util.HashMap;
import java.util.Map;

public class ReportType {
	private String name;
	private String label;
	private boolean xmlFormat;
	private static Map<String, ReportType> types = new HashMap<String, ReportType>();
	public static final ReportType XML = createReportType("xml", "Magic Assistant XML", true);
	public static final ReportType CSV = createReportType("csv", "Magic Assistant CSV");
	public static final ReportType TEXT_DECK_CLASSIC = createReportType("classic", "Deck Classic (Text)");
	public static final ReportType USER_DEFINED = createReportType("user", "User Defined");
	public static final ReportType TABLE_PIPED = createReportType("table", "Piped Table");

	private ReportType(String key, String label, boolean xml) {
		this.name = key;
		this.label = label;
		this.xmlFormat = xml;
		types.put(key, this);
	}

	public static ReportType createReportType(String key, String label) {
		return createReportType(key, label, false);
	}

	public static ReportType createReportType(String key, String label, boolean xml) {
		ReportType reportType = types.get(key);
		if (reportType != null)
			return reportType;
		return new ReportType(key, label, xml);
	}

	/**
	 * Return true if given format is table format. Table format can have header.
	 */
	public boolean isXmlFormat() {
		return xmlFormat;
	}

	@Override
	public String toString() {
		return name;
	}

	public String getLabel() {
		return label;
	}

	public static ReportType valueOf(String key) {
		if (key == null)
			return null;
		return types.get(key);
	}
}
