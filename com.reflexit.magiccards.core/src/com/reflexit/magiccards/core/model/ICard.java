package com.reflexit.magiccards.core.model;

public interface ICard extends Cloneable {
	Object getObjectByField(ICardField field);

	String getName();

	ICard cloneCard();
}
