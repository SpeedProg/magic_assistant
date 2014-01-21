package com.reflexit.magiccards.core.model;

import java.util.HashSet;

public class FieldOwnUniqueAggregator extends AbstractIntPropAggregator {
	private static final MagicCardFieldPhysical FI = MagicCardFieldPhysical.OWN_UNIQUE;

	private FieldOwnUniqueAggregator() {
		super(FI.name());
	}

	static FieldOwnUniqueAggregator instance = new FieldOwnUniqueAggregator();

	public static FieldOwnUniqueAggregator getInstance() {
		return instance;
	}

	@Override
	public int visit(MagicCard card, Object data) {
		if (data == null)
			return 0;
		HashSet<IMagicCard> uniq = (HashSet<IMagicCard>) data;
		if (card.isOwn()) {
			uniq.add(card);
		}
		return 0;
	}

	@Override
	public int visit(MagicCardPhysical card, Object data) {
		if (data == null)
			return 0;
		HashSet<IMagicCard> uniq = (HashSet<IMagicCard>) data;
		if (card.isOwn()) {
			uniq.add(card.getBase());
		}
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
