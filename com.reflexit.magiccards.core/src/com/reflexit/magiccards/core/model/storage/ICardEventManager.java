package com.reflexit.magiccards.core.model.storage;

import com.reflexit.magiccards.core.model.events.ICardEventListener;

public interface ICardEventManager<T> {
	public void addListener(ICardEventListener lis);

	public void removeListener(ICardEventListener lis);

	/**
	 * card values were updated
	 * @param card
	 */
	public void update(T card);
}