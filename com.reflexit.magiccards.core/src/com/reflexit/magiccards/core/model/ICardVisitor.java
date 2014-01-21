package com.reflexit.magiccards.core.model;

public interface ICardVisitor {
	int visit(CardGroup cardGroup, Object data);

	int visit(MagicCardPhysical card, Object data);

	int visit(MagicCard card, Object data);

	int visit(ICard card, Object data);
}