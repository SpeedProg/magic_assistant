package com.reflexit.magiccards.core.model.aggr;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.ICardGroup;
import com.reflexit.magiccards.core.model.MagicCardField;

public class FieldProggressAggregator extends AbstractGroupAggregator {
	public FieldProggressAggregator(ICardField field) {
		super(field);
	}

	public int getTotal(ICard element) {
		if (element instanceof ICardGroup) {
			CardGroup cardGroup = (CardGroup) element;
			int size = getSetSize(cardGroup);
			return size;
		}
		return 1;
	}

	protected int getSetSize(CardGroup cardGroup) {
		return cardGroup.getInt(MagicCardField.UNIQUE_COUNT);
	}

	@Override
	public Object visit(ICard card, Object data) {
		int size = getTotal(card);
		int count = getProgressSize(card);
		float per = 0;
		if (size > 0) {
			per = count * 100 / (float) size;
		}
		if (card instanceof CardGroup) // XXX
			((CardGroup) card).set(field, per);
		return per;
	}

	public int getProgressSize(ICard element) {
		if (element instanceof ICardGroup) {
			CardGroup cardGroup = (CardGroup) element;
			int count = cardGroup.getOwnUnique();
			return count;
		}
		return 0;
	}
}
