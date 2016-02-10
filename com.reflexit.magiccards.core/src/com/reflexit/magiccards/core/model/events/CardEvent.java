package com.reflexit.magiccards.core.model.events;

import java.util.Iterator;

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
	private Object extra;

	public CardEvent(Object source, int type, Object data) {
		this(source, type, data, null);
	}

	public CardEvent(Object source, int type, Object data, Object extra) {
		this.source = source;
		this.type = type;
		this.data = data;
		this.extra = extra;
		//System.err.println(toString());
	}

	public final Object getSource() {
		return this.source;
	}

	public final Object getData() {
		return this.data;
	}

	public final Object getFirstDataElement() {
		Object data = getData();
		while (data instanceof Iterable) {
			Iterable arr = (Iterable) data;
			Iterator iterator = arr.iterator();
			if (iterator.hasNext()) {
				data = iterator.next();
			} else {
				break;
			}
		}
		return data;
	}

	public final Object getExtra() {
		return this.extra;
	}

	public final int getType() {
		return this.type;
	}

	@Override
	public String toString() {
		return "event: " + getTypeStr() + " from " + source + " data " + data;
	}

	private String getTypeStr() {
		switch (type) {
			case ADD:
				return "ADD";
			case REMOVE:
				return "REMOVE";
			case ADD_CONTAINER:
				return "ADD_CONTAINER";
			case REMOVE_CONTAINER:
				return "REMOVE_CONTAINER";
			case UPDATE:
				return "UPDATE";
			case RENAME_CONTAINER:
				return "RENAME_CONTAINER";
			case UPDATE_CONTAINER:
				return "UPDATE_CONTAINER";
			default:
				break;
		}
		return "?";
	}
}
