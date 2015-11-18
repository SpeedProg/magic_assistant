package com.reflexit.magiccards.core.model.abs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CardList<T extends ICard> implements ICardList<T> {
	protected List<T> list;

	public CardList(T single) {
		list = Collections.singletonList(single);
	}

	public CardList(T[] array) {
		this(Arrays.asList(array));
	}

	@Override
	public Iterator<T> iterator() {
		return list.iterator();
	}

	@SuppressWarnings("unchecked")
	public CardList(Iterable<? extends T> iterable) {
		if (iterable == null) {
			this.list = Collections.emptyList();
		} else
			synchronized (iterable) {
				if (iterable instanceof ArrayList) {
					this.list = (ArrayList<T>) ((ArrayList<T>) iterable).clone();
				} else {
					this.list = copyList(iterable);
				}
			}
	}

	@SuppressWarnings("unchecked")
	public CardList(ArrayList<? extends T> iterable) {
		this.list = (ArrayList<T>) ((ArrayList<T>) iterable).clone();
	}

	public <F> List<F> getAll(ICardField f) {
		ArrayList<F> set = new ArrayList<F>();
		for (T card : this) {
			@SuppressWarnings("unchecked")
			F value = (F) card.get(f);
			set.add(value);
		}
		return set;
	}

	public <F> Set<F> getUnique(ICardField f) {
		LinkedHashSet<F> set = new LinkedHashSet<F>();
		for (T card : this) {
			@SuppressWarnings("unchecked")
			F value = (F) card.get(f);
			set.add(value);
		}
		return set;
	}

	public void setAll(ICardField f, Object value) {
		for (T card : this) {
			((ICardModifiable) card).set(f, value);
		}
	}

	@Override
	public int size() {
		return getList().size();
	}

	public List<T> getList() {
		return list;
	}

	public static <T> List<T> copyList(Iterable<? extends T> iterable) {
		ArrayList<T> list = new ArrayList<T>();
		for (T card : iterable) {
			list.add(card);
		}
		return list;
	}

	@Override
	public T get(int k) {
		return getList().get(k);
	}
}
