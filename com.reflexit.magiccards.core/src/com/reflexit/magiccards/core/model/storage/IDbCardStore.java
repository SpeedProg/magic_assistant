package com.reflexit.magiccards.core.model.storage;

import java.util.Collection;

public interface IDbCardStore<T> extends ICardStore<T> {
	Collection<T> getCandidates(String name);

	T getPrime(String name);
}
