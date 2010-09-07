package com.reflexit.magiccards.core.model;

public interface ICardField {
	public Class getType();

	public boolean isTransient();

	public String name();
}
