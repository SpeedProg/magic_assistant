package com.reflexit.magiccards.core.model.aggr;

import com.reflexit.magiccards.core.model.AbstractMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;

public class FieldCount4Aggregator extends AbstractIntAggregator {
	public FieldCount4Aggregator(MagicCardField magicCardField) {
		super(magicCardField);
	}

	@Override
	protected Object visitAbstractMagicCard(AbstractMagicCard card, Object data) {
		int c = card.getOwnCount();
		if (c > 4)
			return 4;
		return c;
	}
}
