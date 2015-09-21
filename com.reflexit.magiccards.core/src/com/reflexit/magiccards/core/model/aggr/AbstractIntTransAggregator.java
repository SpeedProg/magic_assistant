package com.reflexit.magiccards.core.model.aggr;

import java.util.Iterator;

import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.abs.ICardVisitor;

public class AbstractIntTransAggregator extends AbstractGroupTransAggregator implements ICardVisitor {
	public AbstractIntTransAggregator(ICardField field) {
		super(field);
	}

	@Override
	public Object visitIterable(Iterable group, Object data1) {
		int sum = 0;
		Object data = pre(group);
		for (Iterator<ICard> iterator = group.iterator(); iterator.hasNext();) {
			ICard object = iterator.next();
			Object value = object.accept(this, data);
			if (value == null)
				continue;
			sum += ((Integer) value);
		}
		Object value = post(data);
		if (value != null)
			return sum + (Integer) value;
		return sum;
	}
}