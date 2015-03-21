package com.reflexit.magiccards.core.model.expr;

import java.util.regex.Pattern;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.utils.SearchStringTokenizer;
import com.reflexit.magiccards.core.model.utils.SearchStringTokenizer.SearchToken;
import com.reflexit.magiccards.core.model.utils.SearchStringTokenizer.TokenType;

public class BinaryExpr extends Expr {
	final Expr left;
	final Expr right;
	final Operation op;

	public BinaryExpr(Expr left, Operation op, Expr right) {
		if (right == null || left == null || op == null)
			throw new NullPointerException();
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
		return new BinaryExpr(new CardFieldExpr(field), Operation.EQUALS, new Value(value));
	}

	public static BinaryExpr fieldMatches(ICardField field, String value) {
		return new BinaryExpr(new CardFieldExpr(field), Operation.MATCHES, new Value(value));
	}

	public static BinaryExpr fieldMatches(ICardField field, Value value) {
		return new BinaryExpr(new CardFieldExpr(field), Operation.MATCHES, value);
	}

	public static BinaryExpr fieldOp(ICardField field, Operation op, String value) {
		return new BinaryExpr(new CardFieldExpr(field), op, new Value(value));
	}

	@Override
	public boolean evaluate(Object o) {
		switch (this.op) {
			case AND:
				if (this.left.evaluate(o) == false)
					return false;
				return this.right.evaluate(o);
			case OR:
				if (this.left.evaluate(o) == true)
					return true;
				return this.right.evaluate(o);
			case EQUALS: {
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
			}
			case MATCHES:
				return evalutateMatches(o);
			case EQ:
			case LE:
			case GE: {
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
				throw new IllegalArgumentException();
			}
			default:
				throw new UnsupportedOperationException();
		}
	}

	boolean evalutateMatches(Object o) {
		Object x = this.left.getFieldValue(o);
		Object y = this.right.getFieldValue(o);
		if (x == null && y == null)
			return true;
		if (x == null || y == null)
			return false;
		if (this.left instanceof CardFieldExpr && o instanceof IMagicCard && this.right instanceof TextValue) {
			return ((IMagicCard) o)
					.matches(((CardFieldExpr) this.left).getField(), (TextValue) this.right);
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
			return new BinaryExpr(Expr.TRUE, Operation.AND, Expr.TRUE);
		} else if (value.startsWith(">=")) {
			return fieldOp(field, Operation.GE, value.substring(2).trim());
		} else if (value.startsWith("<=")) {
			return fieldOp(field, Operation.LE, value.substring(2).trim());
		} else if (value.startsWith("==")) {
			return fieldOp(field, Operation.EQ, value.substring(2).trim());
		} else if (value.startsWith("=")) {
			return fieldOp(field, Operation.EQ, value.substring(1).trim());
		} else if (value.equals("0")) {
			return new BinaryExpr(Expr.TRUE, Operation.AND, Expr.TRUE);
		} else {
			return fieldOp(field, Operation.EQ, value.trim());
		}
	}

	static public Expr textSearch(ICardField field, String text) {
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
		return res;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + left.hashCode();
		result = prime * result + op.hashCode();
		result = prime * result + right.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof BinaryExpr)) return false;
		BinaryExpr other = (BinaryExpr) obj;
		if (!left.equals(other.left)) return false;
		if (op != other.op) return false;
		if (!right.equals(other.right)) return false;
		return true;
	}
}