package com.reflexit.magiccards.core.model;

public class FieldOwnCountAggregator extends AbstractIntPropAggregator {
	private static final MagicCardField FI = MagicCardField.OWN_COUNT;

	private FieldOwnCountAggregator() {
		super(FI.name());
	}

	static FieldOwnCountAggregator instance = new FieldOwnCountAggregator();

	public static FieldOwnCountAggregator getInstance() {
		return instance;
	}

	@Override
	public int visit(MagicCard card, Object data) {
		return card.getOwnCount();
	}

	@Override
	public int visit(MagicCardPhysical card, Object data) {
		return card.getOwnCount();
	}
}
