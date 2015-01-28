package com.reflexit.magiccards.core.model.abs;

public interface ICardModifiable extends ICard {
	boolean set(ICardField field, Object value);
}
