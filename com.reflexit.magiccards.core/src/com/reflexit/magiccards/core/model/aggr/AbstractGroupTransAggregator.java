package com.reflexit.magiccards.core.model.aggr;

import java.util.Iterator;

import com.reflexit.magiccards.core.model.AbstractMagicCard;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.ICardVisitor;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

public class AbstractGroupTransAggregator implements ICardVisitor {
	protected final ICardField field;

	public AbstractGroupTransAggregator(ICardField field) {
		if (field == null)
			throw new NullPointerException();
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

	protected Object visitGroup(CardGroup group, Object data1) {
		Object sum = null;
		Object data = pre(group);
		for (Iterator<ICard> iterator = group.iterator(); iterator.hasNext();) {
			ICard object = iterator.next();
			Object value = object.accept(this, data);
			sum = aggr(sum, value);
		}
		Object value = post(data);
		sum = aggr(sum, value);
		return cast(sum);
	}

	public Object cast(Object res) {
		return res;
	}

	protected Object visitAbstractMagicCard(AbstractMagicCard card, Object data) {
		if (card instanceof MagicCard) {
			return visitMagicCard((MagicCard) card, data);
		}
		if (card instanceof MagicCardPhysical) {
			return visitMagicCardPhysical((MagicCardPhysical) card, data);
		}
		return card.get(field);
	}

	protected Object visitMagicCardPhysical(MagicCardPhysical card, Object data) {
		return card.get(field);
	}

	protected Object visitMagicCard(MagicCard card, Object data) {
		return card.get(field);
	}

	protected Object post(Object data) {
		return null;
	}

	protected Object pre(CardGroup group) {
		return null;
	}

	protected Object aggr(Object sum, Object value) {
		if (value == null)
			return sum;
		return value;
	}
}