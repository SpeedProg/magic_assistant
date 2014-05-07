package com.reflexit.magiccards.core.model.aggr;

import java.util.Iterator;

import com.reflexit.magiccards.core.model.AbstractMagicCard;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardField;

public class AbstractPowerAggregator extends AbstractFloatAggregator {
	public AbstractPowerAggregator(ICardField field) {
		super(field);
	}

	@Override
	protected Object visitGroup(CardGroup group, Object data) {
		float sum = 0;
		for (Iterator<ICard> iterator = group.iterator(); iterator.hasNext();) {
			ICard card = iterator.next();
			float x;
			if (card instanceof CardGroup)
				x = card.getFloat(field);
			else
				x = card.getFloat(field) * ((AbstractMagicCard) card).getCount();
			sum += x;
		}
		return String.valueOf(sum);
	}

	@Override
	public Object visit(ICard card, Object data) {
		if (card instanceof CardGroup)
			return visitGroup((CardGroup) card, data);
		if (card instanceof AbstractMagicCard) {
			float x = card.getFloat(field) * ((AbstractMagicCard) card).getCount();
			return String.valueOf(x);
		}
		return null;
	}
}
