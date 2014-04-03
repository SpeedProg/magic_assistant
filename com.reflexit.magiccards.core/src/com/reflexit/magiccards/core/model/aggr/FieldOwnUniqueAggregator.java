package com.reflexit.magiccards.core.model.aggr;

import java.util.HashSet;

import com.reflexit.magiccards.core.model.AbstractMagicCard;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;

public class FieldOwnUniqueAggregator extends AbstractIntAggregator {
	private static final MagicCardField FI = MagicCardField.OWN_UNIQUE;

	private FieldOwnUniqueAggregator() {
		super(FI);
	}

	static FieldOwnUniqueAggregator instance = new FieldOwnUniqueAggregator();

	public static FieldOwnUniqueAggregator getInstance() {
		return instance;
	}

	@Override
	protected Object visitAbstractMagicCard(AbstractMagicCard card, Object data) {
		if (data == null)
			return 0;
		HashSet<IMagicCard> uniq = (HashSet<IMagicCard>) data;
		if (card.isOwn()) {
			uniq.add(card.getBase());
		}
		return null;
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
