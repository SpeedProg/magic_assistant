package com.reflexit.magiccards.core.model;

import java.util.HashMap;
import java.util.Iterator;

public class MagicCardFilter {
	private Expr root;
	private int limit = 1000;
	private int sortIndex = 1;
	private boolean ascending;
	public static class Expr {
		boolean translated = false;

		public boolean evaluate(Object o) {
			return false;
		}

		public Object getFieldValue(Object o) {
			return null;
		}
	}
	public static Expr TRUE = new Expr() {
		@Override
		public boolean evaluate(Object o) {
			return true;
		}

		@Override
		public Object getFieldValue(Object o) {
			return Boolean.TRUE;
		}

		@Override
		public String toString() {
			return "true";
		}
	};

	public Expr getRoot() {
		return this.root;
	}
	public static class Node extends Expr {
		String name;

		Node(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return this.name;
		}

		@Override
		public Object getFieldValue(Object o) {
			return this.name;
		}
	}
	public static class Field extends Expr {
		int num;

		Field(int index) {
			this.num = index;
		}

		@Override
		public String toString() {
			return "f" + this.num;
		}

		@Override
		public Object getFieldValue(Object o) {
			if (o instanceof IMagicCard) {
				return ((IMagicCard) o).getObjectByIndex(this.num);
			}
			return null;
		}
	}
	static class Value extends Node {
		Value(String name) {
			super(name);
		}

		@Override
		public String toString() {
			return "'" + this.name + "'";
		}
	}
	public static class BinaryExpr extends Expr {
		Expr left;
		Expr right;
		Operation op;

		BinaryExpr(Expr left, Operation op, Expr right) {
			this.left = left;
			this.right = right;
			this.op = op;
		}

		@Override
		public String toString() {
			return this.left + " " + this.op + " " + this.right;
		}

		public Expr getLeft() {
			return this.left;
		}

		public Expr getRight() {
			return this.right;
		}

		public Operation getOp() {
			return this.op;
		}

		public static BinaryExpr fieldEquals(int index, String value) {
			return new BinaryExpr(new Field(index), Operation.EQUALS, new Value(value));
		}

		public static BinaryExpr fieldMatches(int index, String value) {
			return new BinaryExpr(new Field(index), Operation.MATCHES, new Value(value));
		}

		public static BinaryExpr fieldOp(int index, Operation op, String value) {
			return new BinaryExpr(new Field(index), op, new Value(value));
		}

		@Override
		public boolean evaluate(Object o) {
			if (this.op == Operation.AND) {
				boolean res = this.left.evaluate(o);
				if (res == false)
					return false;
				return this.right.evaluate(o);
			} else if (this.op == Operation.OR) {
				boolean res = this.left.evaluate(o);
				if (res == true)
					return true;
				return this.right.evaluate(o);
			} else if (this.op == Operation.NOT) {
				boolean res = this.left.evaluate(o);
				return !res;
			}
			if (!this.translated) {
				return translate(this).evaluate(o);
			}
			if (this.op == Operation.EQUALS) {
				Object x = this.left.getFieldValue(o);
				Object y = this.right.getFieldValue(o);
				if (x == null && y == null)
					return true;
				if (x == null || y == null)
					return false;
				return x.equals(y);
			} else if (this.op == Operation.MATCHES) {
				Object x = this.left.getFieldValue(o);
				Object y = this.right.getFieldValue(o);
				if (x == null && y == null)
					return true;
				if (x == null || y == null)
					return false;
				if (x instanceof String && y instanceof String) {
					return ((String) x).matches((String) y);
				}
				return false;
			} else if (this.op == Operation.EQ || this.op == Operation.LE || this.op == Operation.GE) {
				Object x = this.left.getFieldValue(o);
				Object y = this.right.getFieldValue(o);
				if (x == null && y == null)
					return true;
				if (x == null || x.equals(""))
					x = "0";
				if (y == null || y.equals(""))
					y = "0";
				if (x.equals(y))
					return true;
				String sx = x.toString();
				String sy = y.toString();
				try {
					float dx = Float.parseFloat(sx);
					float dy = Float.parseFloat(sy);
					if (this.op == Operation.EQ)
						return Float.compare(dx, dy) == 0;
					if (this.op == Operation.GE)
						return dx >= dy;
					if (this.op == Operation.LE)
						return dx <= dy;
				} catch (NumberFormatException e) {
					return false;
				}
				return false;
			}
			return true;
		}

		public static Expr fieldInt(int index, String value) {
			if (value.startsWith(">=")) {
				return fieldOp(index, Operation.GE, value.substring(2).trim());
			} else if (value.startsWith("<=")) {
				return fieldOp(index, Operation.LE, value.substring(2).trim());
			} else if (value.startsWith("=")) {
				return fieldOp(index, Operation.EQ, value.substring(2).trim());
			} else if (value.equals("0")) {
				return fieldOp(index, Operation.GE, value);
			}
			return null;
		}
	}

	static BinaryExpr ignoreCase1Search(int field, String value) {
		String altValue = value;
		char c = value.charAt(0);
		if (Character.isUpperCase(c)) {
			altValue = Character.toLowerCase(c) + value.substring(1);
		} else if (Character.isLowerCase(c)) {
			altValue = Character.toUpperCase(c) + value.substring(1);
		}
		BinaryExpr b1 = BinaryExpr.fieldMatches(field, ".*\\Q" + value + "\\E.*");
		BinaryExpr b2 = BinaryExpr.fieldMatches(field, ".*\\Q" + altValue + "\\E.*");
		BinaryExpr res = new BinaryExpr(b1, Operation.OR, b2);
		return res;
	}

	static Expr textSearch(int field, String text) {
		text = text.trim();
		text = text.replaceAll("\\s\\s*", " ");
		text = text.replaceAll("['\"%]", "_");
		String[] split = text.split(" ");
		Expr res = null;
		for (int i = 0; i < split.length; i++) {
			String value = split[i];
			BinaryExpr cur;
			if (value.startsWith("-") && value.length() > 1) {
				value = value.substring(1);
				cur = ignoreCase1Search(field, value);
				cur = new BinaryExpr(cur, Operation.NOT, null);
			}
			cur = ignoreCase1Search(field, value);
			res = createAndGroup(res, cur);
		}
		return res;
	}

	private static Expr translate(BinaryExpr bin) {
		Expr res = bin;
		String requestedId = bin.getLeft().toString();
		String value = bin.getRight().toString();
		if (Colors.getInstance().getIdPrefix().equals(requestedId)) {
			String en = Colors.getInstance().getEncodeByName(value);
			if (en != null) {
				res = BinaryExpr.fieldMatches(IMagicCard.INDEX_COST, ".*\\Q{" + en + "}\\E.*");
			} else if (value.equals("Multi-Color")) {
				res = BinaryExpr.fieldEquals(IMagicCard.INDEX_CTYPE, "multi");
			} else if (value.equals("Colorless")) {
				BinaryExpr b1 = BinaryExpr.fieldEquals(IMagicCard.INDEX_CTYPE, "colorless");
				BinaryExpr b2 = BinaryExpr.fieldEquals(IMagicCard.INDEX_CTYPE, "land");
				res = new BinaryExpr(b1, Operation.OR, b2);
			}
		} else if (CardTypes.getInstance().getIdPrefix().equals(requestedId)) {
			res = ignoreCase1Search(IMagicCard.INDEX_TYPE, value);
		} else if (Editions.getInstance().getIdPrefix().equals(requestedId)) {
			res = BinaryExpr.fieldEquals(IMagicCard.INDEX_EDITION, value);
		} else if (SuperTypes.getInstance().getIdPrefix().equals(requestedId)) {
			BinaryExpr b1 = BinaryExpr.fieldMatches(IMagicCard.INDEX_TYPE, ".*" + value + " .*");
			BinaryExpr b2 = BinaryExpr.fieldMatches(IMagicCard.INDEX_TYPE, ".*" + value + " -.*");
			res = new BinaryExpr(b1, Operation.AND, new BinaryExpr(b2, Operation.NOT, null));
		} else if (FilterHelper.SUBTYPE.equals(requestedId)) {
			res = textSearch(IMagicCard.INDEX_TYPE, value);
		} else if (FilterHelper.TEXT_LINE.equals(requestedId)) {
			res = textSearch(IMagicCard.INDEX_ORACLE, value);
		} else if (FilterHelper.NAME_LINE.equals(requestedId)) {
			res = textSearch(IMagicCard.INDEX_NAME, value);
		} else if (FilterHelper.CCC.equals(requestedId)) {
			res = BinaryExpr.fieldInt(IMagicCard.INDEX_CMC, value);
		} else if (FilterHelper.POWER.equals(requestedId)) {
			res = BinaryExpr.fieldInt(IMagicCard.INDEX_POWER, value);
		} else if (FilterHelper.TOUGHNESS.equals(requestedId)) {
			res = BinaryExpr.fieldInt(IMagicCard.INDEX_TOUGHNESS, value);
		} else if (FilterHelper.LOCATION.equals(requestedId)) {
			res = BinaryExpr.fieldEquals(MagicCardPhisical.INDEX_LOCATION, value);
		} else if (FilterHelper.RARITY.equals(requestedId)) {
			res = BinaryExpr.fieldEquals(IMagicCard.INDEX_RARITY, value);
			// TODO: Other
		} else {
			res = bin;
		}
		res.translated = true;
		return res;
	}
	public static class Operation {
		public static final Operation AND = new Operation("AND");
		public static final Operation OR = new Operation("OR");
		public static final Operation EQUALS = new Operation("eq");
		public static final Operation MATCHES = new Operation("matches");
		public static final Operation NOT = new Operation("NOT");
		public static final Operation GE = new Operation(">=");
		public static final Operation LE = new Operation("<=");
		public static final Operation EQ = new Operation("==");
		String name;

		Operation(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}

	public void update(HashMap map) {
		Expr expr = createOrGroup(map, Colors.getInstance());
		expr = createAndGroup(createOrGroup(map, CardTypes.getInstance()), expr);
		expr = createAndGroup(createOrGroup(map, SuperTypes.getInstance()), expr);
		expr = createAndGroup(createOrGroup(map, Editions.getInstance()), expr);
		expr = createAndGroup(createOrGroup(map, Locations.getInstance()), expr);
		expr = createAndGroup(createOrGroup(map, Rarity.getInstance()), expr);
		expr = createAndGroup(createTextSearch(map, FilterHelper.SUBTYPE), expr);
		expr = createAndGroup(createTextSearch(map, FilterHelper.TEXT_LINE), expr);
		expr = createAndGroup(createTextSearch(map, FilterHelper.NAME_LINE), expr);
		expr = createAndGroup(createNumericSearch(map, FilterHelper.POWER), expr);
		expr = createAndGroup(createNumericSearch(map, FilterHelper.TOUGHNESS), expr);
		expr = createAndGroup(createNumericSearch(map, FilterHelper.CCC), expr);
		this.root = expr;
	}

	private Expr createTextSearch(HashMap map, String fieldId) {
		Expr sub = null;
		String valueKey = FilterHelper.getPrefConstant(fieldId, FilterHelper.TEXT_POSTFIX);
		String value = (String) map.get(valueKey);
		if (value != null && value.length() > 0) {
			sub = new BinaryExpr(new Node(fieldId), Operation.EQUALS, new Node(value));
		}
		return sub;
	}

	private Expr createNumericSearch(HashMap map, String fieldId) {
		Expr sub = null;
		String valueKey = FilterHelper.getPrefConstant(fieldId, FilterHelper.NUMERIC_POSTFIX);
		String value = (String) map.get(valueKey);
		if (value != null && value.length() > 0) {
			sub = new BinaryExpr(new Node(fieldId), Operation.EQUALS, new Node(value));
		}
		return sub;
	}

	private static Expr createAndGroup(Expr first, Expr expr) {
		if (expr == null)
			return first;
		if (first == null)
			return expr;
		BinaryExpr and = new BinaryExpr(first, Operation.AND, expr);
		return and;
	}

	private Expr createOrGroup(Expr or, Expr res) {
		if (res == null) {
			res = or;
		} else {
			res = new BinaryExpr(or, Operation.OR, res);
		}
		return res;
	}

	private Expr createOrGroup(HashMap map, ISearchableProperty sp) {
		Expr res = null;
		for (Iterator iterator = sp.getIds().iterator(); iterator.hasNext();) {
			String id = (String) iterator.next();
			String value = (String) map.get(id);
			BinaryExpr or = null;
			if (value != null && value.equals("true")) {
				or = new BinaryExpr(new Node(sp.getIdPrefix()), Operation.EQUALS, new Node(sp.getNameById(id)));
			} else if (value != null && value.length() > 0) {
				or = new BinaryExpr(new Node(sp.getIdPrefix()), Operation.EQUALS, new Value(value));
			}
			if (or == null)
				continue;
			res = createOrGroup(or, res);
		}
		return res;
	}

	public int getLimit() {
		return this.limit;
	}

	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}

	public boolean isAscending() {
		return this.ascending;
	}

	/**
	 * sort index column, starting at 1
	 * 
	 * @param sortIndex
	 */
	public void setSortIndex(int sortIndex) {
		this.sortIndex = sortIndex;
	}

	public int getSortIndex() {
		return this.sortIndex;
	}

	void setLimit(int limit) {
		this.limit = limit;
	}

	public boolean isFiltered(Object o) {
		if (this.root == null)
			return false;
		boolean res = !this.root.evaluate(o);
		return res;
	}
}
