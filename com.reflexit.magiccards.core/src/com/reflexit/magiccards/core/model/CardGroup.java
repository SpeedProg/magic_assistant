/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia. All rights reserved. This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alena
 * 
 */
public class CardGroup extends MagicCardHash implements ICardGroup {
	private final String name;
	private ICardField groupField;
	private List<ICard> children;
	private Map<String, CardGroup> subs;

	public static final class NonGroupPredicate implements Predicate<Object> {
		@Override
		public boolean test(Object o) {
			return !(o instanceof ICardGroup);
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
		for (Iterator iterator = children.iterator(); iterator.hasNext();) {
			Object o = iterator.next();
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

	public void sort(Comparator comparator) {
		if (comparator == null)
			return;
		if (children.size() > 1)
			Collections.sort(children, comparator);
		for (Iterator<CardGroup> iterator = subs.values().iterator(); iterator.hasNext();) {
			CardGroup o = iterator.next();
			o.sort(comparator);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.ICardGroup#getChildrenList()
	 */
	@Override
	public List<? extends ICard> getChildrenList() {
		return children;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.ICardGroup#getChildren()
	 */
	@Override
	public synchronized ICard[] getChildren() {
		return ((List<ICard>) getChildrenList()).toArray(new ICard[children.size()]);
	}

	public synchronized Iterator iterator() {
		return children.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.ICardGroup#size()
	 */
	@Override
	public synchronized int size() {
		return this.children.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.ICardGroup#add(com.reflexit.magiccards .core.model.ICard)
	 */
	@Override
	public synchronized void add(ICard elem) {
		this.children.add(elem);
		if (elem instanceof CardGroup) {
			CardGroup cardGroup = (CardGroup) elem;
			subs.put(cardGroup.getName(), (CardGroup) elem);
		}
		rehash();
	}

	public void addToSubGroup(String subGroupName, ICard elem) {
		CardGroup g = getSubGroup(subGroupName);
		if (g == null) {
			g = new CardGroup(getFieldIndex(), subGroupName);
			add(g);
		}
		g.add(elem);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.ICardGroup#remove(java.lang.Object)
	 */
	@Override
	public synchronized void remove(ICard elem) {
		// children.remove(elem); cannot use it, use identify removal
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
		rehash();
	}

	/**
	 * @param index
	 */
	@Override
	public synchronized ICard getChildAtIndex(int index) {
		ICard object = getChildrenList().get(index);
		return object;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getByIndex(int)
	 */
	public synchronized String getLabelByField(ICardField f) {
		if (f == this.groupField)
			return this.name;
		if (children.size() == 0)
			return "";
		Object value = get(f);
		if (value == null)
			return "";
		return String.valueOf(value);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof CardGroup) {
			CardGroup group = (CardGroup) arg0;
			return name.equals(group.name) && children.equals(group.children);
		}
		return false;
	}

	public synchronized void removeEmptyChildren() {
		for (Iterator iterator = children.iterator(); iterator.hasNext();) {
			Object o = iterator.next();
			if (o instanceof CardGroup) {
				CardGroup cardGroup = (CardGroup) o;
				cardGroup.removeEmptyChildren();
				if (cardGroup.children.size() == 0) {
					iterator.remove();
					subs.remove(cardGroup.getName());
				}
			}
		}
	}

	@Override
	public String toString() {
		return name;
	}

	public synchronized CardGroup getSubGroup(String key) {
		return subs.get(key);
	}

	@Override
	public synchronized void clear() {
		children.clear();
		subs.clear();
		rehash();
	}

	public synchronized void rehash() {
		super.clear();
	}

	public synchronized IMagicCardPhysical getFirstCard() {
		if (children.size() == 0)
			return null;
		Object card = children.get(0);
		if (card instanceof CardGroup)
			return ((CardGroup) card).getFirstCard();
		if (card instanceof IMagicCardPhysical)
			return (IMagicCardPhysical) card;
		return null;
	}

	public synchronized boolean contains(IMagicCard card) {
		for (Iterator iterator = children.iterator(); iterator.hasNext();) {
			Object o = iterator.next();
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
		for (Iterator iterator = children.iterator(); iterator.hasNext();) {
			Object o = iterator.next();
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
		if (containsKey(field)) {
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

	public void addAll(Iterable cards) {
		if (cards == null)
			return;
		for (Object elem : cards) {
			children.add((ICard) elem);
			if (elem instanceof CardGroup) {
				CardGroup cardGroup = (CardGroup) elem;
				subs.put(cardGroup.getName(), (CardGroup) elem);
			}
		}
		rehash();
	}

	public int getCreatureCount() {
		return getInt(MagicCardField.CREATURE_COUNT);
	}
}
