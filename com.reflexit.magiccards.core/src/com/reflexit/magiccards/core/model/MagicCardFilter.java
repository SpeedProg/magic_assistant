package com.reflexit.magiccards.core.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.model.MagicCardFilter.SearchToken.TokenType;

public class MagicCardFilter {
	private Expr root;
	private int limit = Integer.MAX_VALUE;
	private SortOrder sortOrder = new SortOrder();
	private ICardField groupFields[];
	private boolean onlyLastSet = false;

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
		ICardField field;

		Field(ICardField field) {
			this.field = field;
		}

		@Override
		public String toString() {
			return "f" + this.field;
		}

		@Override
		public Object getFieldValue(Object o) {
			if (o instanceof IMagicCard) {
				return ((IMagicCard) o).getObjectByField(field);
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

	static class TextValue extends Value {
		public boolean wordBoundary = true;
		public boolean caseSensitive = false;
		public boolean regex = false;

		public TextValue(String name, boolean wordBoundary, boolean caseSensitive, boolean regex) {
			super(name);
			this.wordBoundary = wordBoundary;
			this.caseSensitive = caseSensitive;
			this.regex = regex;
		}

		public void setWordBoundary(boolean b) {
			this.wordBoundary = b;
		}

		public Pattern toPattern() {
			if (regex)
				return Pattern.compile(name);
			int flags = 0;
			if (!caseSensitive)
				flags |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
			if (wordBoundary)
				return Pattern.compile("\\b\\Q" + name + "\\E\\b", flags);
			flags |= Pattern.LITERAL;
			return Pattern.compile(name, flags);
		}

		public String getText() {
			return name;
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
			if (this.op == Operation.NOT) {
				return this.op + " (" + this.left + ")";
			}
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

		public static BinaryExpr fieldEquals(ICardField field, String value) {
			return new BinaryExpr(new Field(field), Operation.EQUALS, new Value(value));
		}

		public static BinaryExpr fieldMatches(ICardField field, String value) {
			return new BinaryExpr(new Field(field), Operation.MATCHES, new Value(value));
		}

		public static BinaryExpr fieldLike(ICardField field, String value) {
			return new BinaryExpr(new Field(field), Operation.LIKE, new Value(value));
		}

		public static BinaryExpr fieldOp(ICardField field, Operation op, String value) {
			return new BinaryExpr(new Field(field), op, new Value(value));
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
				if (x instanceof String && y instanceof String)
					return x.equals(y);
				else
					return x.toString().equals(y.toString());
			} else if (this.op == Operation.MATCHES) {
				return evalutateMatches(o);
			} else if (this.op == Operation.LIKE) {
				return true; // processed by DB
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

		boolean evalutateMatches(Object o) {
			Object x = this.left.getFieldValue(o);
			Object y = this.right.getFieldValue(o);
			if (x == null && y == null)
				return true;
			if (x == null || y == null)
				return false;
			if (this.left instanceof Field && o instanceof IMagicCard && this.right instanceof TextValue) {
				return ((IMagicCard) o).matches(((Field) this.left).field, (TextValue) this.right);
			}
			if (x instanceof String && y instanceof String) {
				String pattern = (String) y;
				String text = (String) x;
				return Pattern.compile(pattern).matcher(text).find();
			}
			return false;
		}

		public static Expr fieldInt(ICardField field, String value) {
			if (value.equals(">= 0")) {
				return TRUE;
			} else if (value.startsWith(">=")) {
				return fieldOp(field, Operation.GE, value.substring(2).trim());
			} else if (value.startsWith("<=")) {
				return fieldOp(field, Operation.LE, value.substring(2).trim());
			} else if (value.startsWith("=")) {
				return fieldOp(field, Operation.EQ, value.substring(2).trim());
			} else if (value.equals("0")) {
				return TRUE;
			}
			return null;
		}
	}

	static class SearchToken {
		static enum TokenType {
			WORD,
			QUOTED,
			REGEX,
			NOT;
		}

		public TokenType getType() {
			return type;
		}

		public String getValue() {
			return value;
		}

		private TokenType type;
		private String value;;

		SearchToken(TokenType type, String value) {
			this.type = type;
			this.value = value;
		}
	}

	public static class SearchStringTokenizer {
		static enum State {
			INIT,
			IN_QUOTE,
			IN_REG
		};

		private CharSequence seq;
		private int cur;
		private State state;

		public void init(CharSequence seq) {
			this.seq = seq;
			this.cur = 0;
			this.state = State.INIT;
		}

		boolean tokenReady = false;
		StringBuffer str;
		SearchToken token = null;

		public SearchToken nextToken() {
			tokenReady = false;
			str = new StringBuffer();
			token = null;
			while (tokenReady == false && cur <= seq.length()) {
				char c = cur < seq.length() ? seq.charAt(cur) : 0;
				switch (state) {
					case INIT:
						switch (c) {
							case '"':
								pushToken(TokenType.WORD);
								state = State.IN_QUOTE;
								break;
							case 'm':
								if (cur + 1 < seq.length() && seq.charAt(cur + 1) == '/') {
									pushToken(TokenType.WORD);
									state = State.IN_REG;
									cur++;
								} else {
									str.append(c);
								}
								break;
							case '-':
								pushToken(TokenType.WORD);
								str.append('-');
								pushToken(TokenType.NOT);
								break;
							case ' ':
							case 0:
								pushToken(TokenType.WORD);
								break;
							default:
								str.append(c);
								break;
						}
						break;
					case IN_REG:
						if (c == '/' || c == 0) {
							pushToken(TokenType.REGEX);
							state = State.INIT;
						} else {
							str.append(c);
						}
						break;
					case IN_QUOTE:
						if (c == '"' || c == 0) {
							pushToken(TokenType.QUOTED);
							state = State.INIT;
						} else {
							str.append(c);
						}
						break;
				}
				cur++;
			}
			return token;
		}

		private void pushToken(TokenType type) {
			if (str.length() > 0) {
				token = new SearchToken(type, str.toString());
				str.delete(0, str.length());
				tokenReady = true;
			}
		}
	}

	static BinaryExpr ignoreCase1SearchDb(ICardField field, String value) {
		char c = value.charAt(0);
		if (Character.isLetter(c)) {
			String altValue = value.replaceAll("['\"%]", "_");
			if (Character.isUpperCase(c)) {
				altValue = Character.toLowerCase(c) + value.substring(1);
			} else if (Character.isLowerCase(c)) {
				altValue = Character.toUpperCase(c) + value.substring(1);
			}
			BinaryExpr b1 = BinaryExpr.fieldLike(field, "%" + value + "%");
			BinaryExpr b2 = BinaryExpr.fieldLike(field, "%" + altValue + "%");
			BinaryExpr res = new BinaryExpr(b1, Operation.OR, b2);
			return res;
		} else {
			return BinaryExpr.fieldLike(field, "%" + value + "%");
		}
	}

	public static BinaryExpr tokenSearch(ICardField field, SearchToken token) {
		String value = token.getValue();
		if (token.getType() == TokenType.REGEX) {
			TextValue tvalue = new TextValue(value, false, false, true);
			return new BinaryExpr(new Field(field), Operation.MATCHES, tvalue);
		} else {
			TextValue tvalue = new TextValue(value, false, false, false);
			char c = value.charAt(0);
			if (Character.isLetter(c) && token.getType() != TokenType.QUOTED) {
				tvalue.setWordBoundary(true);
			}
			return new BinaryExpr(new Field(field), Operation.MATCHES, tvalue);
		}
	}

	static public Expr textSearch(ICardField field, String text) {
		SearchStringTokenizer tokenizer = new SearchStringTokenizer();
		tokenizer.init(text);
		SearchToken token;
		Expr res = null;
		while ((token = tokenizer.nextToken()) != null) {
			BinaryExpr cur;
			if (token.getType() == TokenType.NOT) {
				token = tokenizer.nextToken();
				if (token == null)
					break;
				cur = tokenSearch(field, token);
				cur = new BinaryExpr(cur, Operation.NOT, null);
			} else {
				cur = tokenSearch(field, token);
			}
			res = createAndGroup(res, cur);
		}
		return res;
	}

	private static Expr translate(BinaryExpr bin) {
		Expr res = bin;
		String requestedId = bin.getLeft().toString();
		String value = bin.getRight().toString();
		if (Colors.getInstance().getIdPrefix().equals(requestedId)) {
			String en;
			if (value.equals("Multi-Color")) {
				res = BinaryExpr.fieldEquals(MagicCardField.CTYPE, "multi");
			} else if (value.equals("Mono-Color")) {
				BinaryExpr b1 = BinaryExpr.fieldEquals(MagicCardField.CTYPE, "colorless");
				BinaryExpr b2 = BinaryExpr.fieldEquals(MagicCardField.CTYPE, "mono");
				res = new BinaryExpr(b1, Operation.OR, b2);
			} else if (value.equals("Hybrid")) {
				res = BinaryExpr.fieldEquals(MagicCardField.CTYPE, "hybrid");
			} else if (value.equals("Colorless")) {
				BinaryExpr b1 = BinaryExpr.fieldEquals(MagicCardField.CTYPE, "colorless");
				BinaryExpr b2 = BinaryExpr.fieldEquals(MagicCardField.CTYPE, "land");
				res = new BinaryExpr(b1, Operation.OR, b2);
			} else if ((en = Colors.getInstance().getEncodeByName(value)) != null) {
				res = BinaryExpr.fieldMatches(MagicCardField.COST, ".*" + en + ".*");
			}
		} else if (CardTypes.getInstance().getIdPrefix().equals(requestedId)) {
			res = textSearch(MagicCardField.TYPE, value);
		} else if (Editions.getInstance().getIdPrefix().equals(requestedId)) {
			res = BinaryExpr.fieldEquals(MagicCardField.SET, value);
		} else if (SuperTypes.getInstance().getIdPrefix().equals(requestedId)) {
			BinaryExpr b1 = BinaryExpr.fieldMatches(MagicCardField.TYPE, ".*" + value + " .*");
			BinaryExpr b2 = BinaryExpr.fieldMatches(MagicCardField.TYPE, ".*" + value + " -.*");
			res = new BinaryExpr(b1, Operation.AND, new BinaryExpr(b2, Operation.NOT, null));
		} else if (FilterField.TYPE_LINE.toString().equals(requestedId)) {
			res = textSearch(MagicCardField.TYPE, value);
		} else if (FilterField.NAME_LINE.toString().equals(requestedId)) {
			res = textSearch(MagicCardField.NAME, value);
		} else if (FilterField.CCC.toString().equals(requestedId)) {
			res = BinaryExpr.fieldInt(MagicCardField.CMC, value);
		} else if (FilterField.POWER.toString().equals(requestedId)) {
			res = BinaryExpr.fieldInt(MagicCardField.POWER, value);
		} else if (FilterField.TOUGHNESS.toString().equals(requestedId)) {
			res = BinaryExpr.fieldInt(MagicCardField.TOUGHNESS, value);
		} else if (FilterField.LOCATION.toString().equals(requestedId)) {
			res = BinaryExpr.fieldEquals(MagicCardFieldPhysical.LOCATION, value);
		} else if (FilterField.RARITY.toString().equals(requestedId)) {
			res = BinaryExpr.fieldEquals(MagicCardField.RARITY, value);
		} else if (FilterField.COUNT.toString().equals(requestedId)) {
			res = BinaryExpr.fieldInt(MagicCardFieldPhysical.COUNT, value);
		} else if (MagicCardFieldPhysical.FORTRADECOUNT.name().equals(requestedId)) {
			res = BinaryExpr.fieldInt(MagicCardFieldPhysical.FORTRADECOUNT, value);
		} else if (FilterField.DBPRICE.toString().equals(requestedId)) {
			BinaryExpr b1 = new BinaryExpr(new Field(MagicCardField.DBPRICE), Operation.EQ, new Value("0"));
			res = new BinaryExpr(b1, Operation.AND, BinaryExpr.fieldInt(MagicCardFieldPhysical.PRICE, value));
			res = new BinaryExpr(res, Operation.OR, BinaryExpr.fieldInt(MagicCardField.DBPRICE, value));
		} else if (FilterField.COMMUNITYRATING.toString().equals(requestedId)) {
			res = BinaryExpr.fieldInt(MagicCardField.RATING, value);
		} else if (FilterField.COLLNUM.toString().equals(requestedId)) {
			res = BinaryExpr.fieldInt(MagicCardField.COLLNUM, value);
		} else if (FilterField.ARTIST.toString().equals(requestedId)) {
			res = textSearch(MagicCardField.ARTIST, value);
		} else if (FilterField.PRICE.toString().equals(requestedId)) {
			BinaryExpr b1 = new BinaryExpr(new Field(MagicCardFieldPhysical.PRICE), Operation.EQ, new Value("0"));
			res = new BinaryExpr(b1, Operation.AND, BinaryExpr.fieldInt(MagicCardField.DBPRICE, value));
			res = new BinaryExpr(res, Operation.OR, BinaryExpr.fieldInt(MagicCardFieldPhysical.PRICE, value));
		} else if (FilterField.COMMENT.toString().equals(requestedId)) {
			res = textSearch(MagicCardFieldPhysical.COMMENT, value);
		} else if (MagicCardFieldPhysical.SPECIAL.name().equals(requestedId)) {
			res = textSearch(MagicCardFieldPhysical.SPECIAL, value);
		} else if (FilterField.OWNERSHIP.toString().equals(requestedId)) {
			BinaryExpr b1 = BinaryExpr.fieldEquals(MagicCardFieldPhysical.OWNERSHIP, value);
			Expr b2;
			if ("true".equals(value))
				b2 = BinaryExpr.fieldInt(MagicCardFieldPhysical.OWN_COUNT, ">=1");
			else
				b2 = BinaryExpr.fieldInt(MagicCardFieldPhysical.OWN_COUNT, "==0");
			res = new BinaryExpr(b1, Operation.OR, b2);
		} else if (FilterField.LANG.toString().equals(requestedId)) {
			if (value.equals("")) {
				res = TRUE;
			} else if (value.equals(Languages.Language.ENGLISH.getLang())) {
				res = BinaryExpr.fieldEquals(MagicCardField.LANG, null);
				res = new BinaryExpr(res, Operation.OR, BinaryExpr.fieldEquals(MagicCardField.LANG, value));
			} else {
				res = BinaryExpr.fieldEquals(MagicCardField.LANG, value);
			}
		} else if (requestedId.startsWith(FilterField.TEXT_LINE.toString())) {
			res = textSearch(MagicCardField.TEXT, value);
			res = new BinaryExpr(res, Operation.OR, textSearch(MagicCardField.ORACLE, value));
			if (requestedId.contains("_exclude_")) {
				res = new BinaryExpr(res, Operation.NOT, null);
			}
			// TODO: Other
		} else {
			res = bin;
		}
		res.translated = true;
		return res;
	}

	public static enum Operation {
		AND("AND"),
		OR("OR"),
		EQUALS("eq"),
		MATCHES("matches"),
		NOT("NOT"),
		GE(">="),
		LE("<="),
		EQ("=="),
		LIKE("LIKE"), ;
		private String name;

		Operation(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}

	public void update(HashMap map) {
		Expr expr;
		if (map.containsKey(ColorTypes.AND_ID)) {
			map.remove(ColorTypes.AND_ID);
			expr = createAndGroup(map, Colors.getInstance());
		} else if (map.containsKey(ColorTypes.ONLY_ID)) {
			map.remove(ColorTypes.ONLY_ID);
			expr = createAndNotGroup(map, Colors.getInstance());
		} else {
			expr = createOrGroup(map, Colors.getInstance());
		}
		expr = createAndGroup(createOrGroup(map, ColorTypes.getInstance()), expr);
		expr = createAndGroup(createOrGroup(map, CardTypes.getInstance()), expr);
		expr = createAndGroup(createOrGroup(map, SuperTypes.getInstance()), expr);
		expr = createAndGroup(createOrGroup(map, Editions.getInstance()), expr);
		expr = createAndGroup(createOrGroup(map, Locations.getInstance()), expr);
		expr = createAndGroup(createOrGroup(map, Rarity.getInstance()), expr);
		expr = createAndGroup(createTextSearch(map, FilterField.LANG), expr);
		expr = createAndGroup(createTextSearch(map, FilterField.TYPE_LINE), expr);
		expr = createAndGroup(createTextSearch(map, FilterField.NAME_LINE), expr);
		expr = createAndGroup(createNumericSearch(map, FilterField.POWER), expr);
		expr = createAndGroup(createNumericSearch(map, FilterField.TOUGHNESS), expr);
		expr = createAndGroup(createNumericSearch(map, FilterField.CCC), expr);
		expr = createAndGroup(createNumericSearch(map, FilterField.COUNT), expr);
		expr = createAndGroup(createNumericSearch(map, FilterField.PRICE), expr);
		expr = createAndGroup(createNumericSearch(map, FilterField.DBPRICE), expr);
		expr = createAndGroup(createTextSearch(map, FilterField.COMMENT), expr);
		expr = createAndGroup(createTextSearch(map, FilterField.OWNERSHIP), expr);
		expr = createAndGroup(createNumericSearch(map, FilterField.COMMUNITYRATING), expr);
		expr = createAndGroup(createNumericSearch(map, FilterField.COLLNUM), expr);
		expr = createAndGroup(createTextSearch(map, FilterField.ARTIST), expr);
		expr = createAndGroup(createTextSearch(map, FilterField.SPECIAL), expr);
		expr = createAndGroup(createNumericSearch(map, FilterField.FORTRADECOUNT), expr);
		// text fields
		Expr text = createTextSearch(map, FilterField.TEXT_LINE);
		text = createOrGroup(text, createTextSearch(map, FilterField.TEXT_LINE_2));
		text = createOrGroup(text, createTextSearch(map, FilterField.TEXT_LINE_3));
		expr = createAndGroup(expr, text);
		expr = createAndGroup(createTextSearch(map, FilterField.TEXT_NOT_1), expr);
		expr = createAndGroup(createTextSearch(map, FilterField.TEXT_NOT_2), expr);
		expr = createAndGroup(createTextSearch(map, FilterField.TEXT_NOT_3), expr);
		this.root = expr;
	}

	private Expr createTextSearch(HashMap map, FilterField fieldId) {
		Expr sub = null;
		String valueKey = FilterField.getPrefConstant(fieldId, FilterField.TEXT_POSTFIX);
		String value = (String) map.get(valueKey);
		if (value != null && value.length() > 0) {
			sub = new BinaryExpr(new Node(fieldId.toString()), Operation.EQUALS, new Node(value));
		}
		return sub;
	}

	private Expr createNumericSearch(HashMap map, FilterField fieldId) {
		Expr sub = null;
		String valueKey = FilterField.getPrefConstant(fieldId, FilterField.NUMERIC_POSTFIX);
		String value = (String) map.get(valueKey);
		if (value != null && value.length() > 0) {
			sub = new BinaryExpr(new Node(fieldId.toString()), Operation.EQUALS, new Node(value));
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
		} else if (or != null) {
			res = new BinaryExpr(or, Operation.OR, res);
		}
		return res;
	}

	private Expr createOrGroup(HashMap map, ISearchableProperty sp) {
		return createGroup(map, sp, true, false);
	}

	private Expr createAndGroup(HashMap map, ISearchableProperty sp) {
		return createGroup(map, sp, false, false);
	}

	private Expr createAndNotGroup(HashMap map, ISearchableProperty sp) {
		return createGroup(map, sp, false, true);
	}

	private Expr createGroup(HashMap map, ISearchableProperty sp, boolean orOp, boolean notOp) {
		Expr res = null;
		for (Iterator iterator = sp.getIds().iterator(); iterator.hasNext();) {
			String id = (String) iterator.next();
			String value = (String) map.get(id);
			BinaryExpr or = null;
			if (value != null && value.equals("true")) {
				or = new BinaryExpr(new Node(sp.getIdPrefix()), Operation.EQUALS, new Node(sp.getNameById(id)));
			} else if (value == null || value.equals("false")) {
				if (notOp) {
					or = new BinaryExpr(new Node(sp.getIdPrefix()), Operation.EQUALS, new Node(sp.getNameById(id)));
					or = new BinaryExpr(or, Operation.NOT, null);
				} else {
					// skip or = null;
				}
			} else if (value.length() > 0) {
				or = new BinaryExpr(new Node(sp.getIdPrefix()), Operation.EQUALS, new Value(value));
			}
			if (or == null) {
				continue;
			}
			if (orOp)
				res = createOrGroup(or, res);
			else
				res = createAndGroup(or, res);
		}
		return res;
	}

	public int getLimit() {
		return this.limit;
	}

	/**
	 * sort field
	 * 
	 * @param sortField
	 */
	public void setSortField(ICardField sortField, boolean accending) {
		sortOrder.setSortField(sortField, accending);
	}

	public SortOrder getSortOrder() {
		return this.sortOrder;
	}

	public ICardField getGroupField() {
		if (groupFields != null)
			return groupFields[0];
		return null;
	}

	public ICardField[] getGroupFields() {
		return this.groupFields;
	}

	public void setGroupField(ICardField groupField) {
		setGroupField(0, groupField);
	}

	public void setGroupField(int index, ICardField groupField) {
		if (groupFields == null)
			groupFields = new ICardField[10];
		this.groupFields[index] = groupField;
	}

	public void setLimit(int limit) {
		if (limit < 0)
			throw new IllegalArgumentException("Invalid value for limit (must be >=0)");
		this.limit = limit;
	}

	public boolean isFiltered(Object o) {
		if (this.root == null)
			return false;
		boolean res = !this.root.evaluate(o);
		return res;
	}

	public boolean isOnlyLastSet() {
		return onlyLastSet;
	}

	public void setOnlyLastSet(boolean onlyLastSet) {
		this.onlyLastSet = onlyLastSet;
	}

	public void setNoSort() {
		sortOrder.clear();
	}

	public void setGroupFields(ICardField[] fields) {
		if (fields == null)
			groupFields = null;
		else
			groupFields = Arrays.copyOf(fields, fields.length);
	}
}
