package com.reflexit.magiccards.core.model.storage;

import java.util.Collection;

public interface ICardStore<T> extends ICardSet<T>, IMergeable<T>, ICardEventManager<T>, ILocatable, IStorageContainer<T> {
	public String getName();

	public String getComment();

	public boolean isVirtual();

	public T getCard(int id);

	public Collection<T> getCards(int id);

	public void initialize();

	public void reindex();

	public Collection<T> getCards();

	public void reload();
}