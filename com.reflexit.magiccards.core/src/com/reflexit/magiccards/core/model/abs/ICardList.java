package com.reflexit.magiccards.core.model.abs;

public interface ICardList<E> extends Iterable<E> {
	int size();

	E get(int i);
}
