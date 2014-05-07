package com.reflexit.magiccards.core.model;

public interface ICardVisitor {
	Object visit(ICard card, Object data);
}