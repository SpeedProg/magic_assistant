package com.reflexit.magiccards.core.model.aggr;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardField;

public class FieldProggress4Aggregator extends FieldProggressAggregator {
	public FieldProggress4Aggregator(ICardField field) {
		super(field);
	}

	@Override
	public int getProgressSize(CardGroup cardGroup) {
		return cardGroup.getInt(MagicCardField.COUNT4);
	}

	@Override
	public int getTotal(CardGroup element) {
		return getSetSize(element) * 4;
	}
}
