package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

@SuppressWarnings("serial")
public class SortOrder extends ArrayList<MagicCardComparator> implements Comparator {
	public int compare(Object o1, Object o2) {
		if (o1 == o2)
			return 0; // this is only case it is 0
		SortOrder sortOrder = this;
		int dir = sortOrder.isAccending() ? 1 : -1;
		for (int i = sortOrder.size() - 1; i >= 0; i--) {
			MagicCardComparator elem = sortOrder.get(i);
			int d = elem.compare(o1, o2);
			if (d != 0)
				return d;
		}
		// everything is equal try id's
		if (o1 instanceof IMagicCard && o2 instanceof IMagicCard) {
			IMagicCard c1 = (IMagicCard) o1;
			IMagicCard c2 = (IMagicCard) o2;
			if (c1.getCardId() != 0 && c2.getCardId() != 0) {
				return dir * (c1.getCardId() - c2.getCardId());
			}
		}
		// everything is equal try name
		if (o1 instanceof ICard && o2 instanceof ICard) {
			String name1 = (String) (((ICard) o1).getObjectByField(MagicCardField.NAME));
			String name2 = (String) ((ICard) o2).getObjectByField(MagicCardField.NAME);
			if (name1 != null && name2 != null)
				return dir * name1.compareTo(name2);
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
		SortOrder sortOrder = this;
		MagicCardComparator elem = new MagicCardComparator(sortField, accending);
		if (sortOrder.contains(elem)) {
			sortOrder.remove(elem);
		}
		while (sortOrder.size() > 5) {
			sortOrder.remove(5);
		}
		sortOrder.push(elem);
	}

	public boolean hasSortField(ICardField sortField) {
		if (size() == 0)
			return false;
		for (Iterator<MagicCardComparator> iterator = iterator(); iterator.hasNext();) {
			MagicCardComparator comp = iterator.next();
			if (sortField.equals(comp.getField())) {
				return true;
			}
		}
		return false;
	}

	public boolean isAccending(ICardField sortField) {
		if (size() == 0)
			return true;
		for (Iterator<MagicCardComparator> iterator = iterator(); iterator.hasNext();) {
			MagicCardComparator comp = iterator.next();
			if (sortField.equals(comp.getField())) {
				return comp.isAccending();
			}
		}
		return false; // default to false
	}

	public boolean isAccending() {
		if (size() == 0)
			return true;
		MagicCardComparator elem = peek();
		return elem.isAccending();
	}

	public boolean isTop(ICardField sortField) {
		if (size() == 0)
			return false;
		MagicCardComparator elem = peek();
		return elem.getField().equals(sortField);
	}

	private MagicCardComparator peek() {
		return get(size() - 1);
	}
}
