package com.reflexit.magiccards.core.model.expr;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.abs.ICardField;

public class CardFieldExpr extends Expr {
	private final ICardField field;

	public CardFieldExpr(ICardField field) {
		if (field == null) throw new NullPointerException();
		this.field = field;
	}

	public ICardField getField() {
		return field;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + field.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof CardFieldExpr)) return false;
		CardFieldExpr other = (CardFieldExpr) obj;
		if (!field.equals(other.field)) return false;
		return true;
	}
}