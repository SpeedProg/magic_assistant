package com.reflexit.magiccards.core.model.expr;

public class Value extends Node {
	public Value(String name) {
		super(name);
	}

	@Override
	public String toString() {
		return "'" + name() + "'";
	}
}