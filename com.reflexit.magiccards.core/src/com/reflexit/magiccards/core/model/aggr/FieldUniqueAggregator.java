package com.reflexit.magiccards.core.model.aggr;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import com.reflexit.magiccards.core.model.AbstractMagicCard;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;

public class FieldUniqueAggregator extends AbstractIntTransAggregator {
	public FieldUniqueAggregator(MagicCardField field) {
		super(field);
	}

	@Override
	protected Object visitAbstractMagicCard(AbstractMagicCard card, Object data) {
		if (data == null)
			return 0;
		TIntSet uniq = (TIntHashSet) data;
		int cardId = getUniqueCardId(card);
		if (cardId != -1)
			uniq.add(cardId);
		return 0;
	}

	protected int getUniqueCardId(AbstractMagicCard card) {
		MagicCard base = card.getBase();
		int cardId = base.getEnglishCardId();
		if (cardId == 0)
			cardId = base.getCardId();
		return cardId;
	}

	@Override
	protected Object pre(CardGroup group) {
		return new TIntHashSet();
	}

	@Override
	protected Object post(Object data) {
		if (data == null)
			return 0;
		TIntSet uniq = (TIntSet) data;
		return uniq.size();
	}
}
