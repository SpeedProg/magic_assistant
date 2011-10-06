package com.reflexit.magiccards.core.exports;

import java.util.LinkedHashMap;
import java.util.Map;

public class ReportType {
	private String id;
	private String label;
	private boolean xmlFormat;
	private String extension;
	private static Map<String, ReportType> types = new LinkedHashMap<String, ReportType>();
	public static final ReportType XML = createReportType("xml", "Magic Assistant XML", true);
	public static final ReportType CSV = createReportType("csv", "Magic Assistant CSV");
	public static final ReportType TEXT_DECK_CLASSIC = createReportType("classic", "Deck Classic (Text)", "txt", false);
	public static final ReportType TABLE_PIPED = createReportType("table", "Piped Table");

	private ReportType(String key, String label, boolean xml, String extension) {
		this.id = key;
		this.label = label;
		this.xmlFormat = xml;
		this.extension = extension;
		types.put(key, this);
	}

	public static ReportType createReportType(String key, String label) {
		return createReportType(key, label, key, false);
	}

	public static ReportType createReportType(String key, String label, boolean xml) {
		return createReportType(key, label, key, false);
	}

	public static ReportType createReportType(String key, String label, String extension, boolean xml) {
		ReportType reportType = types.get(key);
		if (reportType != null)
			return reportType;
		return new ReportType(key, label, xml, extension);
	}

	/**
	 * Return true if given format is table format. Table format can have header.
	 */
	public boolean isXmlFormat() {
		return xmlFormat;
	}

	@Override
	public String toString() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public static ReportType valueOf(String key) {
		if (key == null)
			return null;
		return types.get(key);
	}

	public String getExtension() {
		return extension;
	}
}
