package com.reflexit.magiccards.core.model.nav;

import org.eclipse.core.runtime.IPath;

import com.reflexit.magiccards.core.model.ICardDeck;

public class Deck extends CardElement {
	private transient ICardDeck store;

	public Deck(String filename, CardOrganizer parent) {
		super(filename, parent);
	}

	public Deck(String name, IPath path, CardOrganizer parent) {
		super(name, path, parent);
	}

	public ICardDeck getStore() {
		return this.store;
	}

	@Override
	public String getName() {
		return super.getName();
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

	/**
	 * @return
	 */
	public String getFileName() {
		return getPath().lastSegment();
	}

	@Override
	public CardElement newElement(String name, CardOrganizer parent) {
		return new Deck(name + ".xml", parent);
	}
}
