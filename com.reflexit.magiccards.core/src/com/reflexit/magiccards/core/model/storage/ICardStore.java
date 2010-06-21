package com.reflexit.magiccards.core.model.storage;

public interface ICardStore<T> extends ICardSet<T>, IMergeable<T>, ICardEventManager<T>, ILocatable {
	public String getName();

	public String getComment();
}