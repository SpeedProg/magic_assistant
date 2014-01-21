package com.reflexit.magiccards.core.model;

public class FieldCreatureCountAggregator extends AbstractIntPropAggregator {
	private static final String CREATURECOUNT_KEY = "creaturecount";

	private FieldCreatureCountAggregator() {
		super(CREATURECOUNT_KEY);
	}

	static FieldCreatureCountAggregator instance = new FieldCreatureCountAggregator();

	public static FieldCreatureCountAggregator getInstance() {
		return instance;
	}

	@Override
	public int visit(MagicCard card, Object data) {
		if (card.getPower() != null) {
			return 1;
		}
		return 0;
	}

	@Override
	public int visit(MagicCardPhysical card, Object data) {
		if (card.getPower() != null) {
			return card.getCount();
		}
		return 0;
	}
}
