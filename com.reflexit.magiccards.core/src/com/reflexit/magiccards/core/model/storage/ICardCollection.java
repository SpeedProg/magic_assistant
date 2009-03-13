package com.reflexit.magiccards.core.model.storage;

import java.util.Collection;

import com.reflexit.magiccards.core.model.ICardCountable;

public interface ICardCollection<T> extends ICardStore<T>, ILocatable, ICardCountable {
	public T getCard(int id);

	public Collection<T> getCards(int id);
}
