package com.reflexit.magiccards.core.model.expr;

public class NotExpr extends BinaryExpr {
	public NotExpr(Expr left) {
		super(left, Operation.NOT, TRUE);
	}

	@Override
	public boolean evaluate(Object o) {
		return !left.evaluate(o);
	}
}
