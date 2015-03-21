package com.reflexit.magiccards.core.model.expr;

public class Node extends Expr {
	private final String name;

	public Node(String name) {
		if (name == null) throw new NullPointerException();
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

	@Override
	public int hashCode() {
		return 29 + name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Node)) return false;
		Node other = (Node) obj;
		if (!name.equals(other.name)) return false;
		return true;
	}
}