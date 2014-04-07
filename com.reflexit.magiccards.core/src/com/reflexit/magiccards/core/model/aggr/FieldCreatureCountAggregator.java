package com.reflexit.magiccards.core.model.aggr;

import com.reflexit.magiccards.core.model.AbstractMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

public class FieldCreatureCountAggregator extends AbstractIntAggregator {
	public FieldCreatureCountAggregator(MagicCardField magicCardField) {
		super(magicCardField);
	}

	@Override
	protected Object visitAbstractMagicCard(AbstractMagicCard card, Object data) {
		if (card instanceof MagicCard) {
			if (card.getPower() != null) {
				return 1;
			}
		} else if (card instanceof MagicCardPhysical) {
			if (card.getPower() != null) {
				return card.getCount();
			}
		}
		return 0;
	}
}
