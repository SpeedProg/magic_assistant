package com.reflexit.magiccards.core.model;

import java.util.HashSet;

public class FieldUniqueAggregator extends AbstractIntPropAggregator {
	private static final MagicCardField FI = MagicCardField.UNIQUE_COUNT;

	private FieldUniqueAggregator() {
		super(FI.name());
	}

	static FieldUniqueAggregator instance = new FieldUniqueAggregator();

	public static FieldUniqueAggregator getInstance() {
		return instance;
	}

	@Override
	public int visit(MagicCard card, Object data) {
		if (data == null)
			return 0;
		HashSet<IMagicCard> uniq = (HashSet<IMagicCard>) data;
		uniq.add(card);
		return 0;
	}

	@Override
	public int visit(MagicCardPhysical card, Object data) {
		if (data == null)
			return 0;
		HashSet<IMagicCard> uniq = (HashSet<IMagicCard>) data;
		uniq.add(card.getBase());
		return 0;
	}

	@Override
	protected Object pre(CardGroup group) {
		return new HashSet<IMagicCard>();
	}

	@Override
	protected int post(Object data) {
		if (data == null)
			return 0;
		HashSet<IMagicCard> uniq = (HashSet<IMagicCard>) data;
		return uniq.size();
	}
}
