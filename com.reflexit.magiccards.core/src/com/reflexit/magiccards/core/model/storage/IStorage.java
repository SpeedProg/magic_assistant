package com.reflexit.magiccards.core.model.storage;

import java.util.Collection;
import java.util.Iterator;

import com.reflexit.magiccards.core.MagicException;

public interface IStorage<T> {
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
	public boolean removeCard(T o);

	public Iterator<T> cardsIterator();

	public void setAutoSave(boolean value);

	public boolean isAutoCommit();

	public void save();

	public void initialize();
}