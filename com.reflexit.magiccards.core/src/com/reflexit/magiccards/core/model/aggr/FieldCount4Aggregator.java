package com.reflexit.magiccards.core.model.aggr;

import com.reflexit.magiccards.core.model.AbstractMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;


public class FieldCount4Aggregator extends AbstractIntAggregator {
	private FieldCount4Aggregator() {
		super(MagicCardField.COUNT4);
	}

	static FieldCount4Aggregator instance = new FieldCount4Aggregator();

	public static FieldCount4Aggregator getInstance() {
		return instance;
	}

	@Override
	protected Object visitAbstractMagicCard(AbstractMagicCard card, Object data) {
		int c = card.getOwnCount();
		if (c > 4)
			return 4;
		return c;
	}
}
