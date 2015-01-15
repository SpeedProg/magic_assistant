package com.reflexit.magiccards.core.model.aggr;

import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.abs.ICardVisitor;

public class StringAggregator extends CollisionAggregator implements ICardVisitor {
	private static final String MULTI = "*";

	public StringAggregator(ICardField field) {
		super(field, MULTI);
	}

	@Override
	protected Object aggr(Object res, Object value) {
		if (res == null)
			return value;
		if (value == null) {
			if (((String) res).isEmpty())
				return res;
			return MULTI;
		}
		if (value.equals(res))
			return res;
		if (((String) res).isEmpty()) {
			return value;
		} else {
			return MULTI;
		}
	}
}