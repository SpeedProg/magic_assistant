package com.reflexit.magiccards.core.model.aggr;

import java.util.Iterator;

import com.reflexit.magiccards.core.model.AbstractMagicCard;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.abs.ICardVisitor;

public class AbstractFloatAggregator extends AbstractGroupAggregator implements ICardVisitor {
	public AbstractFloatAggregator(ICardField field) {
		super(field);
	}

	@Override
	public Object visitIterable(Iterable group, Object data) {
		float sum = 0;
		for (Iterator<ICard> iterator = group.iterator(); iterator.hasNext();) {
			ICard object = iterator.next();
			float x = getFloat(object);
			sum += x;
		}
		return sum;
	}

	protected float getFloat(ICard card) {
		float x = card.getFloat(field);
		if (x < 0)
			return 0;
		return x;
	}

	@Override
	public Object visit(ICard card, Object data) {
		if (card instanceof CardGroup)
			return visitIterable((CardGroup) card, data);
		if (card instanceof AbstractMagicCard)
			return getFloat(card);
		return null;
	}
}