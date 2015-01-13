package com.reflexit.magiccards.core.model;

public class LegacyField implements ICardField {
	public static LegacyField INSTANCE = new LegacyField();

	protected LegacyField() {
	}
	@Override
	public boolean isTransient() {
		return true;
	}

	@Override
	public String name() {
		return "Legacy";
	}

	@Override
	public String getLabel() {
		return name();
	}

	@Override
	public Object aggregateValueOf(ICard card) {
		return null;
	}

	@Override
	public String getTag() {
		return null;
	}
}
