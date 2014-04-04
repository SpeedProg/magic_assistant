package com.reflexit.magiccards.core.model.aggr;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.ICardGroup;

public class FieldProggress4Aggregator extends FieldProggressAggregator {
	public FieldProggress4Aggregator(ICardField field) {
		super(field);
	}

	@Override
	public int getTotal(ICard element) {
		if (element instanceof ICardGroup) {
			CardGroup cardGroup = (CardGroup) element;
			int size = getSetSize(cardGroup);
			return size * 4;
		}
		return 1;
	}

	@Override
	public int getProgressSize(ICard element) {
		return (Integer) element.accept(FieldCount4Aggregator.getInstance(), null);
	}
}
