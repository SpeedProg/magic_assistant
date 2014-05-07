package com.reflexit.magiccards.core.model.aggr;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCardField;

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
	protected Object visitGroup(CardGroup g, Object data) {
		int size = getTotal(g);
		int count = getProgressSize(g);
		float per = 0;
		if (size > 0) {
			per = count * 100 / (float) size;
		}
		return per;
	}
}
