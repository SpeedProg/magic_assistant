package com.reflexit.magiccards.core.model;

public interface ICardModifiable extends ICard {
	boolean set(ICardField field, String value);
}
