package com.reflexit.magiccards.core.model.events;

public class CardEvent {
	public static final int ADD = 1;
	public static final int REMOVE = 2;
	public static final int ADD_CONTAINER = 3;
	public static final int REMOVE_CONTAINER = 4;
	public static final int UPDATE = 5;
	private Object source;
	private int type;

	public CardEvent(Object source, int type) {
		this.source = source;
		this.type = type;
	}

	public final Object getSource() {
		return this.source;
	}

	public final int getType() {
		return this.type;
	}
}
