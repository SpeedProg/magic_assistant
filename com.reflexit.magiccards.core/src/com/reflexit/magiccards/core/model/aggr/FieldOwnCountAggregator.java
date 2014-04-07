package com.reflexit.magiccards.core.model.aggr;

import com.reflexit.magiccards.core.model.AbstractMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;

public class FieldOwnCountAggregator extends AbstractIntAggregator {
	public FieldOwnCountAggregator(MagicCardField field) {
		super(field);
	}

	@Override
	protected Object visitAbstractMagicCard(AbstractMagicCard card, Object data) {
		return card.getOwnCount();
	}
}
