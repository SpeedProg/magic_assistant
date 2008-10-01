package com.reflexit.magiccards.core.model;

import com.reflexit.magiccards.core.MagicException;

public interface IFilteredCardStore {
	public void update(MagicCardFilter filter) throws MagicException;

	public ICardStore getCardStore();

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

	/**
	 * return top level cards group if grouping is enabled or null if not enabled
	 * @return
	 */
	public CardGroup[] getCardGroups();

	public CardGroup getCardGroup(int index);
}