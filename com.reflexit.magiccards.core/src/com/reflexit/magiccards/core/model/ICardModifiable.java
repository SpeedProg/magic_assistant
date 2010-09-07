package com.reflexit.magiccards.core.model;

public interface ICardModifiable extends ICard {
	boolean setObjectByField(ICardField field, String value);
}
