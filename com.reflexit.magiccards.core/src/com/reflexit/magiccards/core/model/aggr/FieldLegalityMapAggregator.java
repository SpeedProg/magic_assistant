package com.reflexit.magiccards.core.model.aggr;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.LegalityMap;
import com.reflexit.magiccards.core.model.MagicCardField;

public class FieldLegalityMapAggregator extends AbstractStringAggregator {
	public FieldLegalityMapAggregator(MagicCardField f) {
		super(f);
	}

	@Override
	public Object visitGroup(CardGroup g, Object data) {
		Object value = doVisit(g);
		if (value instanceof String) {
			value = LegalityMap.valueOf((String) value);
		}
		g.set(field, value);
		return value;
	}
}
