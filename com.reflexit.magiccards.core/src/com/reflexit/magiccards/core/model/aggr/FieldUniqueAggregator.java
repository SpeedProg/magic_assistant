package com.reflexit.magiccards.core.model.aggr;

import java.util.HashSet;

import com.reflexit.magiccards.core.model.AbstractMagicCard;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;

public class FieldUniqueAggregator extends AbstractIntTransAggregator {
	private static final MagicCardField FI = MagicCardField.UNIQUE_COUNT;

	private FieldUniqueAggregator() {
		super(FI);
	}

	static FieldUniqueAggregator instance = new FieldUniqueAggregator();

	public static FieldUniqueAggregator getInstance() {
		return instance;
	}

	@Override
	protected Object visitAbstractMagicCard(AbstractMagicCard card, Object data) {
		if (data == null)
			return 0;
		HashSet<IMagicCard> uniq = (HashSet<IMagicCard>) data;
		uniq.add(card.getBase());
		return 0;
	}

	@Override
	protected Object pre(CardGroup group) {
		return new HashSet<IMagicCard>();
	}

	@Override
	protected Object post(Object data) {
		if (data == null)
			return 0;
		HashSet<IMagicCard> uniq = (HashSet<IMagicCard>) data;
		return uniq.size();
	}
}
