package com.reflexit.magiccards.core.model.storage;

import java.util.List;

public interface IDbCardStore<T> extends ICardStore<T> {
	List<T> getCandidates(String name);

	T getPrime(String name);
}
