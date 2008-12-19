package com.reflexit.magiccards.core.model.storage;

import com.reflexit.magiccards.core.model.events.ICardEventListener;

public interface ICardStore<T> extends ICardSet<T> {
	public void addListener(ICardEventListener lis);

	public void removeListener(ICardEventListener lis);

	/**
	 * card values were updated
	 * @param card
	 */
	public void update(T card);

	public void setMergeOnAdd(boolean v);

	public boolean getMergeOnAdd();
}