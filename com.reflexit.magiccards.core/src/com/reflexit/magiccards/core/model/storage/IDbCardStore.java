package com.reflexit.magiccards.core.model.storage;

import java.util.List;

import com.reflexit.magiccards.core.model.IMagicCard;

public interface IDbCardStore<T> extends ICardStore<T> {
	List<IMagicCard> getCandidates(String name);
}
