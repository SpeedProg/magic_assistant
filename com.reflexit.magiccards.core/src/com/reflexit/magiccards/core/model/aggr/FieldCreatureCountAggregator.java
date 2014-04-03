package com.reflexit.magiccards.core.model.aggr;

import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

public class FieldCreatureCountAggregator extends AbstractIntAggregator {
	private FieldCreatureCountAggregator() {
		super(MagicCardField.CREATURE_COUNT);
	}

	static FieldCreatureCountAggregator instance = new FieldCreatureCountAggregator();

	public static FieldCreatureCountAggregator getInstance() {
		return instance;
	}

	@Override
	protected Object visitMagicCardPhysical(MagicCardPhysical card, Object data) {
		if (card.getPower() != null) {
			return card.getCount();
		}
		return 0;
	}

	@Override
	protected Object visitMagicCard(MagicCard card, Object data) {
		if (card.getPower() != null) {
			return 1;
		}
		return 0;
	}
}
