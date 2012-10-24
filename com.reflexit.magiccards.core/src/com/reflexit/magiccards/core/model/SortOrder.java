package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Comparator;

@SuppressWarnings("serial")
public class SortOrder implements Comparator {
	private ArrayList<MagicCardComparator> order = new ArrayList<MagicCardComparator>();
	private static int MAX = 7;
	private static int REM = 2;

	public SortOrder() {
		// these are always there
		order.add(new MagicCardComparator(MagicCardField.NAME, true));
		order.add(new MagicCardComparator(MagicCardField.ID, true));
	}

	public int compare(Object o1, Object o2) {
		if (o1 == o2)
			return 0; // this is only case it is 0
		int dir = isAccending() ? 1 : -1;
		for (int i = size() - 1; i >= 0; i--) {
			MagicCardComparator elem = get(i);
			int d = elem.compare(o1, o2);
			if (d != 0)
				return d; // no "dir" since comparator has it already
		}
		return dir * (System.identityHashCode(o1) - System.identityHashCode(o2));
	}

	public Comparator getComparator() {
		return this;
	}

	public void push(MagicCardComparator elem) {
		add(elem);
	}

	public void setSortField(ICardField sortField, boolean accending) {
		MagicCardComparator elem = new MagicCardComparator(sortField, accending);
		for (int i = REM; i < size(); i++) {
			if (elem.equals(get(i))) {
				remove(elem);
				break;
			}
		}
		while (size() >= MAX) {
			order.remove(REM);
		}
		push(elem);
	}

	public MagicCardComparator getComparator(ICardField sortField) {
		int size = size();
		for (int i = REM; i < size; i++) {
			MagicCardComparator elem = get(i);
			if (sortField.equals(elem.getField())) {
				return elem;
			}
		}
		return null;
	}

	public boolean hasSortField(ICardField sortField) {
		return getComparator(sortField) != null;
	}

	public boolean isAccending(ICardField sortField) {
		MagicCardComparator comparator = getComparator(sortField);
		if (comparator != null)
			return comparator.isAccending();
		return false; // default to false
	}

	public boolean isAccending() {
		MagicCardComparator elem = peek();
		return elem.isAccending();
	}

	public boolean isTop(ICardField sortField) {
		MagicCardComparator elem = peek();
		return elem.getField().equals(sortField);
	}

	MagicCardComparator peek() {
		return get(size() - 1);
	}

	public int size() {
		return order.size();
	}

	public MagicCardComparator get(int index) {
		return order.get(index);
	}

	public boolean remove(Object o) {
		return order.remove(o);
	}

	public boolean add(MagicCardComparator e) {
		return order.add(e);
	}

	public boolean isEmpty() {
		return size() <= 2;
	}

	public void clear() {
		int size = size();
		for (int i = size - 1; i >= REM; i--) {
			order.remove(i);
		}
	}
}
