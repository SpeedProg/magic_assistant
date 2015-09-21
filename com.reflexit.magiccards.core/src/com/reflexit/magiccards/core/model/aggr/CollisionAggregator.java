package com.reflexit.magiccards.core.model.aggr;

import java.util.Iterator;

import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;

public class CollisionAggregator extends AbstractGroupAggregator {
	private Object cvalue;

	public CollisionAggregator(ICardField field, Object collisionValue) {
		super(field);
		this.cvalue = collisionValue;
	}

	@Override
	public Object visitIterable(Iterable group, Object data1) {
		Object data = null;
		for (Iterator<ICard> iterator = group.iterator(); iterator.hasNext();) {
			ICard object = iterator.next();
			Object value = object.get(field);
			data = aggr(data, value);
		}
		return data;
	}

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
