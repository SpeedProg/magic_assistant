package com.reflexit.magiccards.core.model.nav;

import org.eclipse.core.runtime.Path;

import com.reflexit.magiccards.core.model.ICardDeck;

public class Deck extends CardElement {
	private ICardDeck store;

	public Deck(String filename, CardOrganizer parent) {
		super(filename, parent);
	}

	public ICardDeck getStore() {
		return this.store;
	}

	public String getName() {
		if (this.store != null)
			return this.store.getDeckName();
		return new Path(getFileName()).removeFileExtension().lastSegment();
	}

	public void open(ICardDeck store) {
		if (this.store == null) {
			this.store = store;
		} else {
			throw new IllegalArgumentException("Already open");
		}
	}

	public void close() {
		this.store = null;
	}

	public boolean isOpen() {
		return this.store != null;
	}
}
