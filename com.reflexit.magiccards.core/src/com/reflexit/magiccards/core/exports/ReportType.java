package com.reflexit.magiccards.core.exports;

public class ReportType {
	private ReportType(String string) {
		this.name = string;
	}
	private String name;
	public static final ReportType XML = new ReportType("XML");
	public static final ReportType CSV = new ReportType("CSV");
	public static final ReportType TEXT_DECK_CLASSIC = new ReportType("Deck Classic");
	public static final ReportType USER_DEFINED = new ReportType("User Defined");
	public static final ReportType TABLE_PIPED = new ReportType("Piped Table");

	@Override
	public String toString() {
		return name;
	}

	public String getLabel() {
		return name;
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
