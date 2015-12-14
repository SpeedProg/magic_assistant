package com.reflexit.magiccards.core.model.aggr;

import java.util.Iterator;

import com.reflexit.magiccards.core.model.AbstractMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.abs.ICardGroup;

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
	public Object visitIterable(Iterable group, Object data1) {
		int sum = 0;
		for (Iterator<ICard> iterator = group.iterator(); iterator.hasNext();) {
			ICard object = iterator.next();
			int value = object.getInt(field);
			if (object instanceof ICardGroup)
				value = object.getInt(field);
			else
				value = (int) visitAbstractMagicCard((AbstractMagicCard) object, data1);
			sum += value;
		}
		return sum;
	}
}
