package com.reflexit.magiccards.core.model.aggr;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardField;

public class FieldProggressAggregator extends AbstractGroupAggregator {
	public FieldProggressAggregator(ICardField field) {
		super(field);
	}

	protected int getSetSize(CardGroup cardGroup) {
		return cardGroup.getInt(MagicCardField.UNIQUE_COUNT);
	}

	public int getProgressSize(CardGroup cardGroup) {
		return cardGroup.getOwnUnique();
	}

	public int getTotal(CardGroup element) {
		return getSetSize(element);
	}

	@Override
	public Object visitIterable(Iterable it, Object data) {
		float per = 0;
		if (it instanceof CardGroup) {
			CardGroup g = (CardGroup) it;
			int size = getTotal(g);
			int count = getProgressSize(g);
			if (size > 0) {
				per = count * 100 / (float) size;
			}
		}
		return per;
	}
}
