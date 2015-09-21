package com.reflexit.magiccards.core.model.aggr;

import java.util.Iterator;

import com.reflexit.magiccards.core.model.LegalityMap;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICard;

public class FieldLegalityMapAggregator extends AbstractGroupAggregator {
	public FieldLegalityMapAggregator(MagicCardField f) {
		super(f);
	}

	@Override
	public Object visitIterable(Iterable group, Object data) {
		LegalityMap res = null;
		for (Iterator<ICard> iterator = group.iterator(); iterator.hasNext();) {
			ICard object = iterator.next();
			Object value = object.get(field);
			res = aggr(res, (LegalityMap) value);
		}
		return res;
	}

	protected LegalityMap aggr(LegalityMap res, LegalityMap value) {
		if (res == null)
			if (value == null)
				return null;
			else
				return value;
		if (value == null)
			return res;
		if (value.equals(res))
			return res;
		return res.merge(value);
	}
}
