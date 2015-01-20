package com.reflexit.magiccards.core.model;

import java.util.Collection;

import com.reflexit.magiccards.core.model.abs.CardList;

@SuppressWarnings("serial")
public class MagicCardList extends CardList {
	public MagicCardList(IMagicCard... array) {
		super(array);
	}

	public MagicCardList(Collection<IMagicCard> list) {
		super(list);
	}

	public MagicCardList(Iterable<IMagicCard> iterable) {
		super(iterable);
	}
}
