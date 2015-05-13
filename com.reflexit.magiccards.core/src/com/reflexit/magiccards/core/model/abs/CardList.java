package com.reflexit.magiccards.core.model.abs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CardList implements ICardList<ICard> {
	protected Iterable<ICard> iterable;
	private boolean copy;

	public CardList(ICard[] array) {
		this(Arrays.asList(array));
	}

	@Override
	public Iterator<ICard> iterator() {
		return iterable.iterator();
	}

	public CardList(Iterable<? extends ICard> iterable) {
		this(iterable, true);
	}

	public CardList(Iterable<? extends ICard> iterable, boolean copy) {
		this.iterable = (Iterable<ICard>) iterable;
		this.copy = copy;
	}

	public List<Object> getAll(ICardField f) {
		ArrayList<Object> set = new ArrayList<Object>();
		for (ICard card : this) {
			Object value = card.get(f);
			set.add(value);
		}
		return set;
	}

	public Set<Object> getUnique(ICardField f) {
		HashSet<Object> set = new HashSet<Object>();
		for (ICard card : this) {
			Object value = card.get(f);
			set.add(value);
		}
		return set;
	}

	public void setAll(ICardField f, Object value) {
		for (ICard card : this) {
			((ICardModifiable) card).set(f, value);
		}
	}

	@Override
	public int size() {
		if (copy == false) {
			int size = 0;
			for (ICard card : iterable) {
				size++;
			}
			return size;
		}
		return getList().size();
	}

	public List<ICard> getList() {
		if (iterable instanceof List)
			return (List<ICard>) iterable;
		if (copy == false)
			return null;
		ArrayList<ICard> list = new ArrayList<ICard>();
		for (ICard card : this) {
			list.add(card);
		}
		iterable = list;
		return list;
	}

	@Override
	public ICard get(int i) {
		return getList().get(i);
	}
}
