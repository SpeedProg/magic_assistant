package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.reflexit.magiccards.core.model.abs.CardList;
import com.reflexit.magiccards.core.model.abs.ICard;

@SuppressWarnings("serial")
public class MagicCardList extends CardList {
	public MagicCardList(IMagicCard... array) {
		super(array);
	}

	public MagicCardList(Collection<IMagicCard> list) {
		super(list);
	}

	public MagicCardList(Iterable<IMagicCard> iterable) {
		super(iterable, true);
	}

	public List<IMagicCard> getMagicBaseList() {
		ArrayList<IMagicCard> set = new ArrayList<IMagicCard>();
		for (ICard card : this) {
			if (card instanceof MagicCardPhysical) {
				set.add(((MagicCardPhysical) card).getBase());
			} else if (card instanceof MagicCard) {
				set.add((IMagicCard) card);
			}
		}
		return set;
	}
}
