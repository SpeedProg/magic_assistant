package com.reflexit.magiccards.core.model.aggr;

import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.ICardField;

public class AbstractIntCountAggregator extends AbstractIntAggregator {
	public AbstractIntCountAggregator(ICardField field) {
		super(field);
	}

	@Override
	protected Object visitMagicCardPhysical(MagicCardPhysical card, Object data) {
		int f = card.getInt(field);
		return f * card.getCount();
	}

	@Override
	protected Object visitMagicCard(MagicCard card, Object data) {
		int f = card.getInt(field);
		return f;
	}
}
