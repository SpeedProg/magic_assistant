package com.reflexit.magiccards.core.model.aggr;

import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

public class AbstractFloatCountAggregator extends AbstractFloatAggregator {
	public AbstractFloatCountAggregator(ICardField field) {
		super(field);
	}

	@Override
	protected Object visitMagicCardPhysical(MagicCardPhysical card, Object data) {
		float f = card.getFloat(field);
		return f * card.getCount();
	}

	@Override
	protected Object visitMagicCard(MagicCard card, Object data) {
		float f = card.getFloat(field);
		return f;
	}
}
