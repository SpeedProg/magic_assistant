package com.reflexit.magiccards.ui.exportWizards;

public class ReportType {
	private ReportType(String string) {
		this.name = string;
	}
	private String name;
	public static final ReportType XML = new ReportType("XML");
	public static final ReportType CSV = new ReportType("CSV");

	@Override
	public String toString() {
		return name;
	}
}
