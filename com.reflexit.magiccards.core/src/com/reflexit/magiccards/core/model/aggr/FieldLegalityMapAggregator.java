package com.reflexit.magiccards.core.model.aggr;

import java.util.Iterator;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.LegalityMap;
import com.reflexit.magiccards.core.model.MagicCardField;

public class FieldLegalityMapAggregator extends AbstractGroupAggregator {
	public FieldLegalityMapAggregator(MagicCardField f) {
		super(f);
	}

	@Override
	protected Object visitGroup(CardGroup group, Object data) {
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
			return (LegalityMap) value.clone();
		if (value == null)
			return res;
		if (value.equals(res))
			return res;
		res.merge(value);
		return res;
	}
}
