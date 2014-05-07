package com.reflexit.magiccards.core.model.aggr;

import java.util.Iterator;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.ICardVisitor;

public class AbstractIntTransAggregator extends AbstractGroupTransAggregator implements ICardVisitor {
	public AbstractIntTransAggregator(ICardField field) {
		super(field);
	}

	@Override
	protected Object visitGroup(CardGroup group, Object data1) {
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
			sum += ((Integer) value);
		return sum;
	}
}