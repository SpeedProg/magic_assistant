package com.reflexit.magiccards.core.model.aggr;

import java.util.Date;

import com.reflexit.magiccards.core.model.abs.ICardField;

public class DateAggregator extends CollisionAggregator {
	public DateAggregator(ICardField field) {
		super(field, null);
	}

	@Override
	protected Object aggr(Object sum, Object value) {
		if (sum == null)
			return value;
		if (value == null)
			return sum;
		if (sum.equals(value))
			return sum;
		Date a = (Date) sum;
		Date b = (Date) value;
		if (a.compareTo(b) > 0)
			return b;
		return a;
	}
}
