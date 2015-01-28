package com.reflexit.magiccards.core.model.abs;

public interface ICard extends Cloneable {
	Object get(ICardField field);

	String getString(ICardField field);

	int getInt(ICardField field);

	float getFloat(ICardField field);

	String getName();

	ICard cloneCard();

	Object accept(ICardVisitor visitor, Object data);
}
