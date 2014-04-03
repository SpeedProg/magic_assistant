package com.reflexit.magiccards.core.model.aggr;

import com.reflexit.magiccards.core.model.ICardField;

public class CollisionAggregator extends AbstractGroupAggregator {
	private Object cvalue;

	public CollisionAggregator(ICardField field, Object collisionValue) {
		super(field);
		this.cvalue = collisionValue;
	}

	@Override
	protected Object aggr(Object sum, Object value) {
		if (sum == null)
			return value;
		if (value == null)
			return sum;
		if (sum.equals(value))
			return sum;
		return cvalue;
	}
}
