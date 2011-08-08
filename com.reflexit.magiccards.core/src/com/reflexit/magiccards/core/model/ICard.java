package com.reflexit.magiccards.core.model;

public interface ICard extends Cloneable {
	Object getObjectByField(ICardField field);

	ICard cloneCard();
}
