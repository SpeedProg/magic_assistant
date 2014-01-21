package com.reflexit.magiccards.core.model;

import java.util.Iterator;

public abstract class AbstractIntPropAggregator implements ICardVisitor {
	private String propName;

	public AbstractIntPropAggregator(String propName) {
		this.propName = propName;
	}

	@Override
	public int visit(ICard card, Object data) {
		return card.accept(this, data);
	}

	public int visit(CardGroup g, Object data) {
		Integer iv = (Integer) g.getProperty(propName);
		if (iv != null) {
			return iv.intValue();
		}
		int i = doVisit(g);
		iv = Integer.valueOf(i);
		g.setProperty(propName, iv);
		return iv.intValue();
	}

	protected int doVisit(CardGroup group) {
		int sum = 0;
		Object data = pre(group);
		for (Iterator<ICard> iterator = group.iterator(); iterator.hasNext();) {
			ICard object = iterator.next();
			int loc = object.accept(this, data);
			sum = aggr(sum, loc);
		}
		int loc = post(data);
		sum = aggr(sum, loc);
		return sum;
	}

	protected int post(Object data) {
		return 0;
	}

	protected Object pre(CardGroup group) {
		return null;
	}

	protected int aggr(int sum, int value) {
		return sum + value;
	}

	public int visit(MagicCardPhysical card, Object data) {
		return 0;
	}

	public int visit(MagicCard card, Object data) {
		return 0;
	}
}