package com.reflexit.magiccards.core.model.aggr;

import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.ICardVisitor;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

public class AbstractFloatAggregator extends AbstractGroupAggregator implements ICardVisitor {
	static class ResultHolder {
		float value;

		public ResultHolder(float value) {
			this.value = value;
		}

		public ResultHolder add(float value) {
			this.value += value;
			return this;
		}
	}

	public AbstractFloatAggregator(ICardField field) {
		super(field);
	}

	@Override
	public Object cast(Object value) {
		if (value instanceof ResultHolder) {
			value = ((ResultHolder) value).value;
		}
		return value;
	}

	@Override
	protected Object aggr(Object sum, Object value) {
		if (value == null)
			return sum;
		if (sum == null) {
			sum = new ResultHolder(0);
		}
		if (value instanceof String) {
			return ((ResultHolder) sum).add(Float.valueOf((String) value));
		}
		return ((ResultHolder) sum).add((Float) value);
	}

	@Override
	protected Object visitMagicCardPhysical(MagicCardPhysical card, Object data) {
		float f = card.getFloat(field);
		return f;
	}

	@Override
	protected Object visitMagicCard(MagicCard card, Object data) {
		float f = card.getFloat(field);
		return f;
	}
}