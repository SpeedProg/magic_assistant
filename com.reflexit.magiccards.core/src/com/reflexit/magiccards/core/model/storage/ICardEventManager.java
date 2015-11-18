package com.reflexit.magiccards.core.model.storage;

import java.util.Set;

import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.events.ICardEventListener;

public interface ICardEventManager<T> {
	public void addListener(ICardEventListener lis);

	public void removeListener(ICardEventListener lis);

	/**
	 * card values were updated
	 * 
	 * @param card
	 * @param mask
	 *            TODO
	 */
	public void update(T card, Set<? extends ICardField> mask);

	public void updateList(Iterable<T> cards, Set<? extends ICardField> mask);
}