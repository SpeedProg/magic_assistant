package com.reflexit.magiccards.core.model.events;

public class CardEvent {
	public static final int ADD = 1;
	public static final int REMOVE = 2;
	public static final int ADD_CONTAINER = 3;
	public static final int REMOVE_CONTAINER = 4;
	public static final int UPDATE = 5;
	public static final int RENAME_CONTAINER = 6;
	public static final int UPDATE_CONTAINER = 7;
	private Object source;
	private int type;
	private Object data;

	public CardEvent(Object source, int type, Object data) {
		this.source = source;
		this.type = type;
		this.data = data;
	}

	public final Object getSource() {
		return this.source;
	}

	public final Object getData() {
		return this.data;
	}

	public final int getType() {
		return this.type;
	}

	@Override
	public String toString() {
		return "event: " + type + " from " + source + " data " + data;
	}
}
