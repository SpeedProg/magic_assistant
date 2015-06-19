package com.reflexit.magiccards.core.model.abs;

import java.util.Collection;
import java.util.Iterator;

public interface ICardSet<T> extends Iterable<T> {
	/**
	 * Total number of cards
	 * 
	 * @return
	 */
	public abstract int size();

	/**
	 * Add a card to physical media
	 * 
	 * @param card
	 * @return
	 */
	public abstract boolean add(T card);

	/**
	 * Add cards to a physical media
	 * 
	 * @param card
	 * @return TODO
	 * @return
	 */
	public abstract boolean addAll(Collection<? extends T> list);

	/**
	 * Remove a card from a physical media
	 * 
	 * @param card
	 * @return
	 */
	public abstract boolean remove(T o);

	/**
	 * Remove cards to a physical media
	 * 
	 * @param card
	 * @return
	 */
	public abstract boolean removeAll(Collection<? extends T> list);

	/**
	 * Remove all cards from collection
	 */
	public abstract boolean removeAll();

	/**
	 * Read-only iterator
	 */
	@Override
	public abstract Iterator<T> iterator();

	public boolean contains(T card);
}