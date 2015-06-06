package com.reflexit.magiccards.core.model.abs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CardList implements ICardList<ICard> {
	protected Iterable<ICard> iterable;

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
		this.iterable = copyList(iterable);
	}

	public <T> List<T> getAll(ICardField f) {
		ArrayList<T> set = new ArrayList<T>();
		for (ICard card : this) {
			T value = (T) card.get(f);
			set.add(value);
		}
		return set;
	}

	public <T> Set<T> getUnique(ICardField f) {
		LinkedHashSet<T> set = new LinkedHashSet<T>();
		for (ICard card : this) {
			T value = (T) card.get(f);
			set.add(value);
		}
		return set;
	}

	public void setAll(ICardField f, Object value) {
		for (ICard card : this) {
			((ICardModifiable) card).set(f, value);
		}
	}

	@SuppressWarnings("unused")
	@Override
	public int size() {
		if (!(iterable instanceof List)) {
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
		return copyList(iterable);
	}

	public static List<ICard> copyList(Iterable<? extends ICard> iterable) {
		ArrayList<ICard> list = new ArrayList<ICard>();
		for (ICard card : iterable) {
			list.add(card);
		}
		return list;
	}

	@Override
	public ICard get(int k) {
		if (!(iterable instanceof List)) {
			int i = 0;
			for (ICard card : iterable) {
				if (i == k) return card;
			}
			throw new ArrayIndexOutOfBoundsException(k);
		}
		return getList().get(k);
	}
}
