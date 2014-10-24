package com.reflexit.magiccards.core.model.expr;

import com.reflexit.magiccards.core.model.FilterField;
import com.reflexit.magiccards.core.model.IMagicCard;

public class FilterFieldExpr extends Expr {
	final private FilterField field;

	public FilterFieldExpr(FilterField field) {
		this.field = field;
	}

	@Override
	public String toString() {
		return this.field.toString();
	}

	@Override
	public Object getFieldValue(Object o) {
		if (o instanceof IMagicCard) {
			return ((IMagicCard) o).get(field.getField());
		}
		return null;
	}

	public FilterField getFilterField() {
		return field;
	}
}