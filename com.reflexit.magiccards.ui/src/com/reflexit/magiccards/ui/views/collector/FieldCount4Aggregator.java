package com.reflexit.magiccards.ui.views.collector;

import com.reflexit.magiccards.core.model.AbstractIntPropAggregator;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

public class FieldCount4Aggregator extends AbstractIntPropAggregator {
	private FieldCount4Aggregator() {
		super("COUNT4");
	}

	static FieldCount4Aggregator instance = new FieldCount4Aggregator();

	public static FieldCount4Aggregator getInstance() {
		return instance;
	}

	@Override
	public int visit(MagicCard card, Object data) {
		int c = card.getOwnCount();
		if (c > 4)
			return 4;
		return c;
	}

	@Override
	public int visit(MagicCardPhysical card, Object data) {
		int c = card.getOwnCount();
		if (c > 4)
			return 4;
		return c;
	}
}
