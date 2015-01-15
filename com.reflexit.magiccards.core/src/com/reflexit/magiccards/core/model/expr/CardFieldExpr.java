package com.reflexit.magiccards.core.model.expr;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.abs.ICardField;

public class CardFieldExpr extends Expr {
	ICardField field;

	public CardFieldExpr(ICardField field) {
		this.field = field;
	}

	@Override
	public String toString() {
		return "f" + this.field;
	}

	@Override
	public Object getFieldValue(Object o) {
		if (o instanceof IMagicCard) {
			return ((IMagicCard) o).get(field);
		}
		return null;
	}
}