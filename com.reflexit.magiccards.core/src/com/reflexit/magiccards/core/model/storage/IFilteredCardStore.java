package com.reflexit.magiccards.core.model.storage;

import java.util.Iterator;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.abs.ICardCountable;
import com.reflexit.magiccards.core.model.abs.ICardGroup;

public interface IFilteredCardStore<T> extends Iterable<T>, ILocatable, ICardCountable {
	public void update() throws MagicException;

	public MagicCardFilter getFilter();

	public ICardStore<T> getCardStore();

	/**
	 * Size of filtered list, top level
	 *
	 * @return
	 */
	public int getSize();

	/**
	 * Number of leaf elements if groups (and size if not groupped)
	 * 
	 * @return
	 */
	public int getFlatSize();

	/**
	 * Elements in filtered list
	 *
	 * @return
	 */
	public Object[] getElements();

	@Override
	public Iterator<T> iterator();

	/**
	 * Returns given element in filtered list
	 *
	 * @param index
	 * @return
	 */
	public Object getElement(int index);

	/**
	 * return top level cards group, if no grouping it will contain all elements
	 *
	 * @return
	 */
	public ICardGroup getCardGroupRoot();

	public boolean contains(T card);

	public void clear();
}