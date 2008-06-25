package com.reflexit.magiccards.core.model;

import com.reflexit.magiccards.core.MagicException;

public interface IFilteredCardStore<T> {
	public void update(MagicCardFilter filter) throws MagicException;

	/**
	 * Size of filtered list
	 * @return
	 */
	public int getSize();

	/**
	 * Elements in filtered list
	 * @return
	 */
	public Object[] getElements();

	/**
	 * Returns given element in filtered list
	 * @param index
	 * @return
	 */
	public Object getElement(int index);

	public ICardStore<T> getCardStore();
}