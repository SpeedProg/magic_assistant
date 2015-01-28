package com.reflexit.magiccards.core.model.expr;

public class Node extends Expr {
	private final String name;

	public Node(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return this.name;
	}

	public String name() {
		return this.name;
	}

	@Override
	public Object getFieldValue(Object o) {
		return this.name;
	}
}