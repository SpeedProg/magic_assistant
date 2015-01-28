package com.reflexit.magiccards.core.model.abs;

public interface ICardVisitor {
	Object visit(ICard card, Object data);
}