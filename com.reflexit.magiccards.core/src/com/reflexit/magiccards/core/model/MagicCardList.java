package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.reflexit.magiccards.core.model.abs.CardList;
import com.reflexit.magiccards.core.model.abs.ICard;

public class MagicCardList extends CardList<IMagicCard> {
	public MagicCardList(ArrayList<? extends IMagicCard> iterable) {
		super(iterable);
	}

	public MagicCardList(IMagicCard single) {
		super(single);
	}

	public MagicCardList(IMagicCard... array) {
		super(array);
	}

	public MagicCardList(Iterable<? extends IMagicCard> iterable) {
		super(iterable);
	}

	public Collection<IMagicCard> getMagicBaseList() {
		LinkedHashSet<IMagicCard> set = new LinkedHashSet<IMagicCard>();
		for (ICard card : this) {
			if (card instanceof MagicCardPhysical) {
				set.add(((MagicCardPhysical) card).getBase());
			} else if (card instanceof MagicCard) {
				set.add((IMagicCard) card);
			}
		}
		return set;
	}

	public Set<Location> getLocations() {
		return getUnique(MagicCardField.LOCATION);
	}

	public List<IMagicCard> getMagicBaseList1() {
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
