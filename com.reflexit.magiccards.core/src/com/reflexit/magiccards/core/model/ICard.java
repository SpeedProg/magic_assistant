package com.reflexit.magiccards.core.model;

public interface ICard extends Cloneable {
	Object get(ICardField field);

	String getName();

	ICard cloneCard();

	int accept(ICardVisitor visitor, Object data);
}
