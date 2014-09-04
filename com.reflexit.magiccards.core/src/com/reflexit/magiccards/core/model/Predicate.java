package com.reflexit.magiccards.core.model;

public interface Predicate<T> {
	public abstract boolean test(T o);
}
