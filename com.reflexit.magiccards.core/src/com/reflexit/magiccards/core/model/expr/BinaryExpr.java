package com.reflexit.magiccards.core.model.expr;

import java.util.regex.Pattern;

import com.reflexit.magiccards.core.model.Colors;
import com.reflexit.magiccards.core.model.FilterField;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Languages;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.utils.SearchStringTokenizer;
import com.reflexit.magiccards.core.model.utils.SearchStringTokenizer.SearchToken;
import com.reflexit.magiccards.core.model.utils.SearchStringTokenizer.TokenType;

public class BinaryExpr extends Expr {
	final Expr left;
	final Expr right;
	final Operation op;
	public static BinaryExpr BTRUE = new BinaryExpr(Expr.TRUE, Operation.AND, Expr.TRUE) {
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

	public BinaryExpr(Expr left, Operation op, Expr right) {
		if (right == null)
			throw new NullPointerException();
		this.left = left;
		this.right = right;
		this.op = op;
	}

	public static BinaryExpr valueOf(Expr expr) {
		if (expr instanceof BinaryExpr)
			return (BinaryExpr) expr;
		if (expr == TRUE || expr == EMPTY)
			return BTRUE;
		return new BinaryExpr(expr, Operation.AND, TRUE);
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
		return new BinaryExpr(new CardFieldExpr(field), Operation.EQUALS, new Value(value));
	}

	public static BinaryExpr fieldMatches(ICardField field, String value) {
		return new BinaryExpr(new CardFieldExpr(field), Operation.MATCHES, new Value(value));
	}

	public static BinaryExpr fieldLike(ICardField field, String value) {
		return new BinaryExpr(new CardFieldExpr(field), Operation.LIKE, new Value(value));
	}

	public static BinaryExpr fieldOp(ICardField field, Operation op, String value) {
		BinaryExpr res = new BinaryExpr(new CardFieldExpr(field), op, new Value(value));
		res.translated = true;
		return res;
	}

	@Override
	public boolean evaluate(Object o) {
		if (!translated) {
			throw new IllegalArgumentException();
		}
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
		if (this.left instanceof CardFieldExpr && o instanceof IMagicCard && this.right instanceof TextValue) {
			return ((IMagicCard) o).matches(((CardFieldExpr) this.left).field, (TextValue) this.right);
		}
		if (x instanceof String && y instanceof String) {
			String pattern = (String) y;
			String text = (String) x;
			return Pattern.compile(pattern).matcher(text).find();
		}
		return false;
	}

	public static BinaryExpr fieldInt(ICardField field, String value) {
		if (value.equals(">= 0")) {
			return new BinaryExpr(MagicCardFilter.TRUE, Operation.AND, MagicCardFilter.TRUE);
		} else if (value.startsWith(">=")) {
			return fieldOp(field, Operation.GE, value.substring(2).trim());
		} else if (value.startsWith("<=")) {
			return fieldOp(field, Operation.LE, value.substring(2).trim());
		} else if (value.startsWith("=")) {
			return fieldOp(field, Operation.EQ, value.substring(2).trim());
		} else if (value.equals("0")) {
			return new BinaryExpr(MagicCardFilter.TRUE, Operation.AND, MagicCardFilter.TRUE);
		} else {
			return fieldOp(field, Operation.EQ, value.trim());
		}
	}

	@Override
	public Expr translate() {
		if (translated)
			return this;
		BinaryExpr bin = this;
		Expr res = null;
		String value = bin.getRight().toString();
		if (bin.getLeft() instanceof FilterFieldExpr) {
			FilterField ff = ((FilterFieldExpr) bin.getLeft()).getFilterField();
			switch (ff) {
				case RARITY:
				case LOCATION:
				case EDITION:
					res = fieldEquals(ff.getField(), value);
					break;
				case TYPE_LINE:
				case NAME_LINE:
				case ARTIST:
				case COMMENT:
				case SPECIAL:
				case CARD_TYPE:
					res = textSearch(ff.getField(), value);
					break;
				case FORMAT:
					TextValue tvalue = new TextValue(value, true, true, false);
					res = new BinaryExpr(new CardFieldExpr(ff.getField()), Operation.MATCHES, tvalue);
					break;
				case CCC:
				case POWER:
				case TOUGHNESS:
				case COUNT:
				case FORTRADECOUNT:
				case COMMUNITYRATING:
				case COLLNUM:
					res = fieldInt(ff.getField(), value);
					break;
				case COLOR: {
					String en;
					if (value.equals("Multi-Color")) {
						res = fieldEquals(MagicCardField.CTYPE, "multi");
					} else if (value.equals("Mono-Color")) {
						BinaryExpr b1 = fieldEquals(MagicCardField.CTYPE, "colorless");
						BinaryExpr b2 = fieldEquals(MagicCardField.CTYPE, "mono");
						res = new BinaryExpr(b1, Operation.OR, b2);
					} else if (value.equals("Hybrid")) {
						res = fieldEquals(MagicCardField.CTYPE, "hybrid");
					} else if (value.equals("Colorless")) {
						BinaryExpr b1 = fieldEquals(MagicCardField.CTYPE, "colorless");
						BinaryExpr b2 = fieldEquals(MagicCardField.CTYPE, "land");
						res = new BinaryExpr(b1, Operation.OR, b2);
					} else if ((en = Colors.getInstance().getEncodeByName(value)) != null) {
						res = fieldMatches(MagicCardField.COST, ".*" + en + ".*");
					}
					break;
				}
				case DBPRICE: {
					BinaryExpr b1 = new BinaryExpr(new CardFieldExpr(MagicCardField.DBPRICE), Operation.EQ, new Value("0"));
					res = new BinaryExpr(b1, Operation.AND, fieldInt(MagicCardField.PRICE, value));
					res = new BinaryExpr(res, Operation.OR, fieldInt(MagicCardField.DBPRICE, value));
					break;
				}
				case PRICE: {
					BinaryExpr b1 = new BinaryExpr(new CardFieldExpr(MagicCardField.PRICE), Operation.EQ, new Value("0"));
					res = new BinaryExpr(b1, Operation.AND, fieldInt(MagicCardField.DBPRICE, value));
					res = new BinaryExpr(res, Operation.OR, fieldInt(MagicCardField.PRICE, value));
					break;
				}
				case OWNERSHIP: {
					BinaryExpr b1 = fieldEquals(MagicCardField.OWNERSHIP, value);
					Expr b2;
					if ("true".equals(value))
						b2 = fieldInt(MagicCardField.OWN_COUNT, ">=1");
					else
						b2 = fieldInt(MagicCardField.OWN_COUNT, "==0");
					res = b1.or(b2);
					break;
				}
				case LANG: {
					if (value.equals("")) {
						res = TRUE;
					} else if (value.equals(Languages.Language.ENGLISH.getLang())) {
						res = fieldEquals(MagicCardField.LANG, null)
								.or(fieldEquals(MagicCardField.LANG, value));
					} else {
						res = fieldEquals(MagicCardField.LANG, value);
					}
					break;
				}
				case TEXT_LINE:
				case TEXT_LINE_2:
				case TEXT_LINE_3:
					res = textSearch(MagicCardField.TEXT, value)
							.or(textSearch(MagicCardField.ORACLE, value));
					break;
				case TEXT_NOT_1:
				case TEXT_NOT_2:
				case TEXT_NOT_3:
					res = textSearch(MagicCardField.TEXT, value)
							.or(textSearch(MagicCardField.ORACLE, value))
							.not();
					break;
				default:
					break;
			}
			if (res != null) {
				return res.translate();
			}
		}
		res = new BinaryExpr(bin.left.translate(), bin.op, bin.right.translate());
		res.translated = true;
		return res;
	}

	static public BinaryExpr textSearch(ICardField field, String text) {
		SearchStringTokenizer tokenizer = new SearchStringTokenizer();
		tokenizer.init(text);
		SearchToken token;
		Expr res = Expr.EMPTY;
		while ((token = tokenizer.nextToken()) != null) {
			BinaryExpr cur;
			if (token.getType() == TokenType.NOT) {
				token = tokenizer.nextToken();
				if (token == null)
					break;
				cur = MagicCardFilter.tokenSearch(field, token);
				cur = new NotExpr(cur);
			} else {
				cur = MagicCardFilter.tokenSearch(field, token);
			}
			res = res.and(cur);
		}
		return valueOf(res);
	}
}