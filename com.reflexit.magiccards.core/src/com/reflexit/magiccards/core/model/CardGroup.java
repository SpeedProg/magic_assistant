/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia. All rights reserved. This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.reflexit.magiccards.core.model.abs.CardList;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.abs.ICardGroup;

/**
 * @author Alena
 *
 */
public final class CardGroup extends MagicCardHash implements ICardGroup, Iterable<ICard> {
	private final String name;
	private ICardField groupField;
	private List<ICard> children;
	private Map<String, CardGroup> subs;
	private MagicCardFilter filter;
	private ICard[] visibleElements;
	private CardGroup parent;

	public static final class NonGroupPredicate implements Predicate<Object> {
		@Override
		public boolean test(Object o) {
			return (o instanceof ICard) && !(o instanceof ICardGroup);
		}
	}

	public CardGroup(ICardField fieldIndex, String name) {
		if (name == null)
			throw new NullPointerException();
		this.groupField = fieldIndex;
		this.name = name;
		this.children = new ArrayList<ICard>(2);
		this.subs = new LinkedHashMap<String, CardGroup>(4);
	}

	@Override
	public MagicCard getBase() {
		return null;
	}

	@Override
	public boolean isPhysical() {
		for (Object o : this) {
			if (o instanceof IMagicCardPhysical) {
				if (!((IMagicCardPhysical) o).isPhysical())
					return false;
			}
		}
		return true;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public synchronized int getOwnTotalAll() {
		return 0; // not supported
	}

	@Override
	public ICardField getFieldIndex() {
		return this.groupField;
	}

	@Override
	public List<? extends ICard> getChildrenList() {
		return Arrays.asList(getChildren());
	}

	@Override
	public synchronized ICard[] getChildren() {
		if (visibleElements == null) {
			if (children.size() == 0) {
				return new ICard[0];
			}
			if (filter == null) {
				visibleElements = children.toArray(new ICard[children.size()]);
			} else {
				visibleElements = filter.filterCards(children);
				SortOrder sortOrder = filter.getSortOrder();
				if (!sortOrder.isEmpty()) {
					Comparator<ICard> comparator = sortOrder.getComparator();
					Arrays.sort(visibleElements, comparator);
				}
			}
		}
		return visibleElements;
	}

	@Override
	public synchronized Iterator<ICard> iterator() {
		return new ArrayIterator<ICard>(getChildren()) {
			private ICard cur;

			@Override
			public ICard next() {
				return cur = super.next();
			}

			@Override
			public void remove() {
				CardGroup.this.remove(cur);
				subs.remove(cur.getName());
				recache();
			}
		};
	}

	public class ArrayIterator<T> implements Iterator<T> {
		private T array[];
		private int pos = 0;

		public ArrayIterator(T anArray[]) {
			array = anArray;
		}

		@Override
		public boolean hasNext() {
			return pos < array.length;
		}

		@Override
		public T next() throws NoSuchElementException {
			if (hasNext())
				return array[pos++];
			else
				throw new NoSuchElementException();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public synchronized int size() {
		return getChildren().length;
	}

	@Override
	public synchronized void add(ICard elem) {
		doAdd(elem);
		recache();
	}

	private void doAdd(ICard elem) {
		this.children.add(elem);
		if (elem instanceof CardGroup) {
			CardGroup cardGroup = (CardGroup) elem;
			subs.put(cardGroup.getName(), (CardGroup) elem);
			cardGroup.parent = this;
		}
	}

	public void addToSubGroup(String subGroupName, ICard elem) {
		CardGroup g = getSubGroup(subGroupName);
		if (g == null) {
			g = new CardGroup(getFieldIndex(), subGroupName);
			add(g);
		}
		g.add(elem);
	}

	@Override
	public synchronized void remove(ICard elem) {
		// children.remove(elem); cannot use it, use identity removal
		for (Iterator iterator = children.iterator(); iterator.hasNext();) {
			Object o = iterator.next();
			if (o == elem) {
				iterator.remove();
				break;
			}
		}
		if (elem instanceof CardGroup) {
			CardGroup cardGroup = (CardGroup) elem;
			subs.remove(cardGroup.getName());
		}
		recache();
	}

	/**
	 * @param index
	 */
	@Override
	public synchronized ICard getChildAtIndex(int index) {
		if (index >= getChildren().length)
			return null;
		ICard object = getChildren()[index];
		return object;
	}

	public synchronized String getLabelByField(ICardField f) {
		if (f == this.groupField)
			return this.name;
		if (getChildren().length == 0)
			return "";
		Object value = get(f);
		if (value == null)
			return "";
		return String.valueOf(value);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof CardGroup))
			return false;
		CardGroup other = (CardGroup) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		return true;
	}

	@Override
	public synchronized CardGroup getSubGroup(String key) {
		return subs.get(key);
	}

	public Collection<CardGroup> getSubGroups() {
		return subs.values();
	}

	@Override
	public synchronized void clear() {
		children.clear();
		subs.clear();
		recache();
	}

	public synchronized void recache() {
		super.clear();
		this.visibleElements = null;
	}

	@Override
	public synchronized IMagicCard getFirstCard() {
		if (getChildren().length == 0)
			return null;
		Object card = getChildren()[0];
		if (card instanceof CardGroup)
			return ((CardGroup) card).getFirstCard();
		if (card instanceof IMagicCard)
			return (IMagicCard) card;
		return null;
	}

	public synchronized boolean contains(IMagicCard card) {
		for (Object o : this) {
			if (o instanceof CardGroup) {
				if (((CardGroup) o).contains(card))
					return true;
			} else {
				if (o == card)
					return true;
				if (o.equals(card))
					return true;
			}
		}
		return false;
	}

	public Collection expand(Collection result, Predicate<Object> filter) {
		if (filter.test(this))
			result.add(this);
		for (Object o : this) {
			if (filter.test(o))
				result.add(o);
			if (o instanceof CardGroup)
				((CardGroup) o).expand(result, filter);
		}
		return result;
	}

	public synchronized Collection<IMagicCard> expand() {
		return expand(new ArrayList<IMagicCard>(), new NonGroupPredicate());
	}

	public static String getGroupName(IMagicCard elem, ICardField ifield) {
		try {
			if (ifield instanceof MagicCardField) {
				MagicCardField field = (MagicCardField) ifield;
				switch (field) {
					case NAME:
						return elem.getEnglishName();
					case COST:
						return Colors.getColorName(elem.getCost());
					case CMC:
						int ccc = elem.getCmc();
						if (ccc == 0 && elem.getType() != null && elem.getType().contains("Land")) {
							return "Land";
						} else {
							return String.valueOf(ccc);
						}
					case LANG:
						String language = elem.getLanguage();
						if (language == null)
							return "English";
						else
							return language;
					case SIDEBOARD:
						if (elem instanceof MagicCardPhysical) {
							if (((MagicCardPhysical) elem).isSideboard()) {
								return "Sideboard";
							}
						}
						return "Main Deck";
					default:
						break;
				}
			}
			return String.valueOf(elem.get(ifield));
		} catch (Exception e) {
			return "Unknown";
		}
	}

	@Override
	public Object get(ICardField field) {
		if (field == MagicCardField.NAME)
			return getName();
		if (size() == 0) {
			if (field == MagicCardField.LEGALITY)
				return LegalityMap.EMPTY;
			return null;
		}
		boolean containsKey = containsKey(field);
		if (containsKey) {
			return super.get(field);
		} else {
			super.set(field, null);
			Object v = field.aggregateValueOf(this);
			super.set(field, v);
			return v;
		}
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	@Override
	public IMagicCard cloneCard() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLocation(Location location) {
		throw new UnsupportedOperationException();
	}

	public Collection<Object> getValues() {
		ArrayList<Object> list = new ArrayList<Object>();
		ICardField[] xfields = MagicCardField.allNonTransientFields(true);
		for (ICardField field : xfields) {
			list.add(get(field));
		}
		return list;
	}

	public synchronized void addAll(Iterable cards) {
		if (cards == null)
			return;
		for (Object elem : new CardList(cards).getList()) {
			doAdd((ICard) elem);
		}
		recache();
	}

	public int getCreatureCount() {
		return getInt(MagicCardField.CREATURE_COUNT);
	}

	@Override
	public boolean isBasicLand() {
		String cost = getCost();
		if (cost != null && cost.length() > 0 && !cost.equals("*"))
			return false;
		if ("*".equals(getLanguage())) {
			for (ICard card : getChildren()) {
				if (card instanceof IMagicCard && ((IMagicCard) card).isBasicLand()) {
					continue;
				}
				return false;
			}
			return true;
		}
		return super.isBasicLand();
	}

	public MagicCardFilter getFilter() {
		return filter;
	}

	public void setFilter(MagicCardFilter filter) {
		this.filter = filter;
		recache();
		for (Iterator<CardGroup> iterator = subs.values().iterator(); iterator.hasNext();) {
			CardGroup o = iterator.next();
			o.setFilter(filter);
		}
	}

	@Override
	public ICardGroup getParent() {
		return parent;
	}

	@Override
	public int depth() {
		if (getParent() == null)
			return 1;
		return getParent().depth() + 1;
	}

	@Override
	public synchronized void removeAll() {
		children.clear();
		subs.clear();
		recache();
	}

	@Override
	public boolean isTransient() {
		return size() == 1 || (size() > 0 && getFieldIndex() == MagicCardField.NAME);
	}
}
