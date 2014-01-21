package com.reflexit.magiccards.core.model;

public interface ICardField {
	public boolean isTransient();

	public String name();

	public String getGroupLabel();

	public Object valueOf(ICard card);
}
