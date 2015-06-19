package com.reflexit.magiccards.core.model.abs;

public interface ICardField {
	public boolean isTransient();

	public String name();

	public String getLabel();

	public Object aggregateValueOf(ICard card);
}
