package com.reflexit.magiccards.core.model.aggr;

import com.reflexit.magiccards.core.model.AbstractMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;

public class FieldOwnUniqueAggregator extends FieldUniqueAggregator {
	public FieldOwnUniqueAggregator(MagicCardField field) {
		super(field);
	}

	@Override
	protected int getUniqueCardId(AbstractMagicCard card) {
		if (!card.isOwn())
			return -1;
		return super.getUniqueCardId(card);
	}
}
