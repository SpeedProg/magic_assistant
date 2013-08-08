package com.reflexit.magiccards.core.model;

import java.util.Arrays;
import java.util.Comparator;

@SuppressWarnings("serial")
public class SortOrder implements Comparator, Cloneable {
	private static int MAX = 7;
	private static int REM = 2;
	private MagicCardComparator order[] = new MagicCardComparator[MAX];
	private int curSize = REM;

	@Override
	protected Object clone() {
		try {
			SortOrder ret = (SortOrder) super.clone();
			ret.order = Arrays.copyOf(order, order.length);
			return ret;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public SortOrder() {
		// these are always there
		order[0] = (new MagicCardComparator(MagicCardField.NAME, true));
		order[1] = (new MagicCardComparator(MagicCardField.ID, true));
	}

	public int compare(Object o1, Object o2) {
		if (o1 == o2)
			return 0; // this is only case it is 0
		int dir = isAccending() ? 1 : -1;
		for (int i = curSize - 1; i >= 0; i--) {
			MagicCardComparator elem = order[i];
			int d = elem.compare(o1, o2);
			if (d != 0)
				return d; // no "dir" since comparator has it already
		}
		return dir * (System.identityHashCode(o1) - System.identityHashCode(o2));
	}

	public Comparator getComparator() {
		return this;
	}

	public void setSortField(ICardField sortField, boolean accending) {
		MagicCardComparator elem = new MagicCardComparator(sortField, accending);
		for (int i = REM; i < curSize; i++) {
			if (elem.equals(order[i])) {
				remove(i);
				break;
			}
		}
		while (curSize >= MAX) {
			remove(REM);
		}
		add(elem);
	}

	public MagicCardComparator getComparator(ICardField sortField) {
		int size = curSize;
		for (int i = REM; i < size; i++) {
			MagicCardComparator elem = order[i];
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
		MagicCardComparator elem = order[curSize - 1];
		return elem.isAccending();
	}

	public boolean isTop(ICardField sortField) {
		MagicCardComparator elem = order[curSize - 1];
		return elem.getField().equals(sortField);
	}

	public int size() {
		return curSize;
	}

	MagicCardComparator peek() {
		return order[curSize - 1];
	}

	public MagicCardComparator get(int index) {
		return order[index];
	}

	private boolean add(MagicCardComparator e) {
		order[curSize] = e;
		curSize++;
		return true;
	}

	public boolean isEmpty() {
		return curSize <= 2;
	}

	public void clear() {
		for (; curSize >= REM; curSize--) {
			order[curSize] = null;
		}
	}

	private MagicCardComparator remove(int index) {
		MagicCardComparator c = order[index];
		for (; index < curSize - 1; index++) {
			order[index] = order[index + 1];
		}
		order[index] = null;
		curSize--;
		return c;
	}
}
