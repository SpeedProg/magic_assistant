package com.reflexit.magiccards.core.model.expr;

public class NotExpr extends BinaryExpr {
	public NotExpr(Expr left) {
		super(left, Operation.NOT, TRUE);
	}

	@Override
	public boolean evaluate(Object o) {
		if (!translated) {
			translate();
		}
		return !left.evaluate(o);
	}

	@Override
	public Expr translate() {
		if (translated)
			return this;
		NotExpr res = new NotExpr(left.translate());
		res.translated = true;
		return res;
	}
}
