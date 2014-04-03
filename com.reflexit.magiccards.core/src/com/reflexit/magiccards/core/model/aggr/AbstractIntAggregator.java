package com.reflexit.magiccards.core.model.aggr;

import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.ICardVisitor;

public class AbstractIntAggregator extends AbstractGroupAggregator implements ICardVisitor {
	static class ResultHolder {
		public ResultHolder(int value) {
			this.value = value;
		}

		int value;

		public ResultHolder add(int value) {
			this.value += value;
			return this;
		}
	}

	public AbstractIntAggregator(ICardField field) {
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
		return ((ResultHolder) sum).add((Integer) value);
	}
}