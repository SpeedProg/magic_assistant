package com.reflexit.magiccards.core.model.aggr;

import java.util.Iterator;

import com.reflexit.magiccards.core.model.AbstractMagicCard;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.ICardVisitor;

public class AbstractFloatAggregator extends AbstractGroupAggregator implements ICardVisitor {
	public AbstractFloatAggregator(ICardField field) {
		super(field);
	}

	@Override
	protected Object visitGroup(CardGroup group, Object data) {
		float sum = 0;
		for (Iterator<ICard> iterator = group.iterator(); iterator.hasNext();) {
			ICard object = iterator.next();
			float x = object.getFloat(field);
			sum += x;
		}
		return sum;
	}

	@Override
	public Object visit(ICard card, Object data) {
		if (card instanceof CardGroup)
			return visitGroup((CardGroup) card, data);
		if (card instanceof AbstractMagicCard)
			return card.getFloat(field);
		return null;
	}
}