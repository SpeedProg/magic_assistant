package com.reflexit.magiccards.core.model.nav;

public class CardCollection extends CardElement {
	public CardCollection(String filename, CardOrganizer parent) {
		super(filename, parent);
	}

	@Override
	public CardElement newElement(String name, CardOrganizer parent) {
		return new CardCollection(name + ".xml", parent);
	}
}
