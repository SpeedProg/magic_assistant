package com.reflexit.magiccards.core.model.storage;

import java.util.Iterator;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardFilter;

public interface IFilteredCardStore<T> extends Iterable<T>, ILocatable {
	public void update(MagicCardFilter filter) throws MagicException;

	public MagicCardFilter getFilter();

	public ICardStore getCardStore();

	/**
	 * Size of filtered list
	 * 
	 * @return
	 */
	public int getSize();

	/**
	 * Elements in filtered list
	 * 
	 * @return
	 */
	public Object[] getElements();

	public Iterator<T> iterator();

	/**
	 * Returns given element in filtered list
	 * 
	 * @param index
	 * @return
	 */
	public Object getElement(int index);

	/**
	 * return top level cards group if grouping is enabled or null if not
	 * enabled
	 * 
	 * @return
	 */
	public CardGroup[] getCardGroups();

	public CardGroup getCardGroup(int index);

	public boolean contains(IMagicCard card);
}