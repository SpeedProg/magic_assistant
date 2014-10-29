package com.reflexit.magiccards.core.model.expr;

public enum Operation {
	AND("AND"),
	OR("OR"),
	EQUALS("eq"),
	MATCHES("matches"),
	NOT("NOT"),
	GE(">="),
	LE("<="),
	EQ("==");
	private String name;

	Operation(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return this.name;
	}
}