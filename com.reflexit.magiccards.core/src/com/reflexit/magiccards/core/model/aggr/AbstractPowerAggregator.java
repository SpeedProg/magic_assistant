package com.reflexit.magiccards.core.model.aggr;

import java.util.Iterator;

import com.reflexit.magiccards.core.model.AbstractMagicCard;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;

public class AbstractPowerAggregator extends AbstractFloatAggregator {
	public AbstractPowerAggregator(ICardField field) {
		super(field);
	}

	@Override
	public Object visitIterable(Iterable group, Object data) {
		float sum = 0;
		for (Iterator<ICard> iterator = group.iterator(); iterator.hasNext();) {
			ICard card = iterator.next();
			float x;
			if (card instanceof CardGroup)
				x = card.getFloat(field);
			else
				x = getPower((AbstractMagicCard) card);
			sum += x;
		}
		return String.valueOf(sum);
	}

	@Override
	public Object visit(ICard card, Object data) {
		if (card instanceof CardGroup)
			return visitIterable((CardGroup) card, data);
		if (card instanceof AbstractMagicCard) {
			float x = getPower((AbstractMagicCard) card);
			return String.valueOf(x);
		}
		return null;
	}

	private float getPower(AbstractMagicCard card) {
		int count = card.getCount();
		float res = card.getFloat(field);
		if (!Float.isNaN(res)) {
			float x = res * count;
			return x;
		}
		return 0;
	}
}
