package com.reflexit.magiccards.core.model.events;

import java.util.Set;

import com.reflexit.magiccards.core.model.abs.CardList;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;

public class CardEventUpdate extends CardEvent {
	public CardEventUpdate(Object source, CardList<? extends ICard> data, Set<? extends ICardField> mask) {
		super(source, UPDATE, data, mask);
	}

	public CardEventUpdate(Object source, ICard data, Set<? extends ICardField> mask) {
		super(source, UPDATE, new CardList<ICard>(data), mask);
	}

	@SuppressWarnings("unchecked")
	public CardList<? extends ICard> getCardList() {
		return (CardList<? extends ICard>) getData();
	}

	public Set<? extends ICardField> getMask() {
		return (Set<? extends ICardField>) getExtra();
	}
}
