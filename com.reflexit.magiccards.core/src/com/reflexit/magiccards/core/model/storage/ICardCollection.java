package com.reflexit.magiccards.core.model.storage;

import com.reflexit.magiccards.core.model.ICardCountable;

public interface ICardCollection<T> extends ICardStore<T>, ILocatable, ICardCountable {
}
