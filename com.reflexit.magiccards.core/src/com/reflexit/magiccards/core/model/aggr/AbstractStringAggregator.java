package com.reflexit.magiccards.core.model.aggr;

import java.util.Iterator;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.ICardVisitor;

public class AbstractStringAggregator extends AbstractGroupAggregator implements ICardVisitor {
	private String MULTI = "*";

	public AbstractStringAggregator(ICardField field) {
		super(field);
	}

	@Override
	public Object visitGroup(CardGroup g, Object data) {
		String value = doVisit(g);
		g.set(field, value);
		return value;
	}

	@Override
	protected String doVisit(CardGroup group) {
		String data = null;
		for (Iterator<ICard> iterator = group.iterator(); iterator.hasNext();) {
			ICard object = iterator.next();
			Object value = object.get(field);
			String s = value == null ? null : String.valueOf(value);
			data = aggr(data, s);
		}
		return data;
	}

	protected String aggr(String res, String value) {
		if (res == null)
			return value;
		if (value == null) {
			if (res.isEmpty())
				return res;
			return MULTI;
		}
		if (value.equals(res))
			return res;
		if (res.length() == 0) {
			return value;
		} else {
			return MULTI;
		}
	}
}