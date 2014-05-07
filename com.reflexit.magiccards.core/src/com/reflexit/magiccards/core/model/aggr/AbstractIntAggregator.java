package com.reflexit.magiccards.core.model.aggr;

import java.util.Iterator;

import com.reflexit.magiccards.core.model.AbstractMagicCard;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

public class AbstractIntAggregator extends AbstractGroupAggregator {
	public AbstractIntAggregator(ICardField field) {
		super(field);
	}

	@Override
	protected Object visitAbstractMagicCard(AbstractMagicCard card, Object data) {
		if (card instanceof MagicCard) {
			return visitMagicCard((MagicCard) card, data);
		}
		if (card instanceof MagicCardPhysical) {
			return visitMagicCardPhysical((MagicCardPhysical) card, data);
		}
		return card.get(field);
	}

	protected Object visitMagicCardPhysical(MagicCardPhysical card, Object data) {
		return card.get(field);
	}

	protected Object visitMagicCard(MagicCard card, Object data) {
		return card.get(field);
	}

	@Override
	protected Object visitGroup(CardGroup group, Object data1) {
		int sum = 0;
		for (Iterator<ICard> iterator = group.iterator(); iterator.hasNext();) {
			ICard object = iterator.next();
			int value = object.getInt(field);
			sum += value;
		}
		return sum;
	}
}