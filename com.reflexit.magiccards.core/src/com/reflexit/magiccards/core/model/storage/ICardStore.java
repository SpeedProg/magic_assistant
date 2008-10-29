package com.reflexit.magiccards.core.model.storage;

import java.util.Collection;
import java.util.Iterator;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.events.ICardEventListener;

public interface ICardStore<T> {
	/**
	 * Total number of cards
	 * @return
	 */
	public int getTotal();

	/**
	 * Add a card to physical media 
	 * @param card
	 * @return
	 */
	public boolean addCard(T card);

	/**
	 * Add cards to a physical media 
	 * @param card
	 * @return
	 */
	public void addAll(Collection<T> list) throws MagicException;

	/**
	 * Remove a card from a physical media 
	 * @param card
	 * @return
	 */
	public void removeCard(T o);

	public Iterator<T> cardsIterator();

	public void addListener(ICardEventListener lis);

	public void removeListener(ICardEventListener lis);

	public void setAutoSave(boolean value);

	public void save();
}