package com.reflexit.magiccards.core.model.aggr;

import com.reflexit.magiccards.core.model.AbstractMagicCard;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.ICardVisitor;

public abstract class AbstractGroupAggregator implements ICardVisitor {
	protected final ICardField field;

	public AbstractGroupAggregator(ICardField field) {
		this.field = field;
	}

	@Override
	public Object visit(ICard card, Object data) {
		if (card instanceof CardGroup)
			return visitGroup((CardGroup) card, data);
		if (card instanceof AbstractMagicCard)
			return visitAbstractMagicCard((AbstractMagicCard) card, data);
		return null;
	}

	protected Object visitGroup(CardGroup g, Object data) {
		Object value = doVisit(g);
		g.set(field, value);
		return value;
	}

	protected Object visitAbstractMagicCard(AbstractMagicCard card, Object data) {
		return card.get(field);
	}

	protected abstract Object doVisit(CardGroup group);
}