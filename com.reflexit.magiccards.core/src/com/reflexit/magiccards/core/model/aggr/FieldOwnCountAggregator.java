package com.reflexit.magiccards.core.model.aggr;

import com.reflexit.magiccards.core.model.AbstractMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;

public class FieldOwnCountAggregator extends AbstractIntAggregator {
	private FieldOwnCountAggregator() {
		super(MagicCardField.OWN_COUNT);
	}

	static FieldOwnCountAggregator instance = new FieldOwnCountAggregator();

	public static FieldOwnCountAggregator getInstance() {
		return instance;
	}

	@Override
	protected Object visitAbstractMagicCard(AbstractMagicCard card, Object data) {
		return card.getOwnCount();
	}
}
