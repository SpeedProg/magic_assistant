package com.reflexit.magiccards.core.model.nav;

import com.reflexit.magiccards.core.model.ICardDeck;
import com.reflexit.magiccards.core.model.IMagicCard;

public class Deck extends CardElement {
	private ICardDeck<IMagicCard> store;

	public Deck(String filename, CardOrganizer parent) {
		super(filename, parent);
	}
}
