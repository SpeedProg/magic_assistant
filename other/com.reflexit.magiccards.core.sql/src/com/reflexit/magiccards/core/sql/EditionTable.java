package com.reflexit.magiccards.core.sql;

public class EditionTable {
	public static final String TABLE = "editions";

	public static String getDbTypeFields() {
		return ""//
				+ "id INT primary key," //
				+ "abbr VARCHAR(5),"//
				+ "name VARCHAR(80)";
	}
}
