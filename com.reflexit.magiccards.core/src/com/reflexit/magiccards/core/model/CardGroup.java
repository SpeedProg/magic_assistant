/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.reflexit.magiccards.core.model.storage.ILocatable;

/**
 * @author Alena
 * 
 */
public class CardGroup implements ICardCountable, ICard, ILocatable {
	private String name;
	private ICardField groupField;
	private int count;
	private List<ICard> children;
	private static final String CREATURECOUNT_KEY = "creaturecount";
	private HashMap<String, Object> props;
	private Map<String, CardGroup> subs;
	private MagicCardPhysical base;

	public CardGroup(ICardField fieldIndex, String name) {
		this.groupField = fieldIndex;
		this.name = name;
		this.children = new ArrayList(2);
		this.subs = new LinkedHashMap<String, CardGroup>(4);
	}

	public synchronized IMagicCard getBase() {
		if (base == null) {
			if (children.size() == 1 && !(children.get(0) instanceof CardGroup))
				return getFirstCard();
			base = new MagicCardPhysical(new MagicCard(), null);
			base.getBase().setName(name);
			base.setOwn(true);
			for (Iterator iterator = children.iterator(); iterator.hasNext();) {
				Object o = iterator.next();
				if (o instanceof CardGroup) {
					addBase(((CardGroup) o).getBase());
				} else if (o instanceof IMagicCard) {
					addBase((IMagicCard) o);
				}
			}
		}
		return base;
	}

	private void addBase(IMagicCard o) {
		ICardField[] allNonTransientFields;
		// if (o instanceof MagicCard)
		// allNonTransientFields = MagicCardField.allNonTransientFields();
		// else
		allNonTransientFields = MagicCardFieldPhysical.allNonTransientFields();
		for (int i = 0; i < allNonTransientFields.length + 1; i++) {
			ICardField field = i == 0 ? MagicCardFieldPhysical.LOCATION : allNonTransientFields[i - 1];
			if (field == MagicCardField.NAME) {
				continue;
			}
			Object value = o.getObjectByField(field);
			Object mine = base.getObjectByField(field);
			Object newmine = null;
			if (mine == null) {
				newmine = value;
			} else {
				if (field == MagicCardField.DBPRICE || field == MagicCardFieldPhysical.PRICE) {
					Float fvalue = (Float) value;
					Float fmain = (Float) mine;
					if (o instanceof MagicCardPhysical) {
						// && ((MagicCardPhysical) o).isOwn()
						int count = ((ICardCountable) o).getCount();
						newmine = fmain + fvalue * count;
					} else {
						newmine = fmain + (fvalue == null ? 0 : fvalue);
					}
				} else if (field == MagicCardField.POWER || field == MagicCardField.TOUGHNESS) {
					Float fvalue = MagicCard.convertFloat((String) value);
					Float fmain = MagicCard.convertFloat((String) mine);
					if (fvalue.isNaN())
						fvalue = 0f;
					if (fmain.isNaN())
						fmain = 0f;
					if (o instanceof ICardCountable) {
						// && ((MagicCardPhysical) o).isOwn()
						int count = ((ICardCountable) o).getCount();
						newmine = fmain + fvalue * count;
					} else {
						newmine = fmain + fvalue;
					}
				} else if (field == MagicCardFieldPhysical.LOCATION) {
					if (mine.equals(value)) {
						// good
					} else {
						newmine = Location.NO_WHERE;
					}
				} else if (field.getType() == String.class) {
					if (mine.equals(value)) {
						// good
					} else {
						newmine = "*";
					}
				} else if (field == MagicCardFieldPhysical.OWNERSHIP) {
					newmine = "false";
				} else {
					// ...
				}
			}
			if (newmine != null) {
				((ICardModifiable) base).setObjectByField(field, String.valueOf(newmine));
			}
		}
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public synchronized int getCount() {
		if (count == 0)
			calculateCount();
		return this.count;
	}

	public synchronized int calculateCount() {
		count = 0;
		for (Iterator iterator = children.iterator(); iterator.hasNext();) {
			Object o = iterator.next();
			if (o instanceof CardGroup) {
				count += ((CardGroup) o).calculateCount();
			} else if (o instanceof ICardCountable) {
				count += ((ICardCountable) o).getCount();
			} else {
				count++;
			}
		}
		return count;
	}

	public synchronized int getCreatureCount() {
		Integer cc = (Integer) getProperty(CREATURECOUNT_KEY);
		if (cc != null) {
			return cc.intValue();
		}
		int cci = 0;
		for (Iterator<ICard> iterator = children.iterator(); iterator.hasNext();) {
			ICard object = iterator.next();
			if (object instanceof IMagicCard) {
				if (((IMagicCard) object).getPower() != null) {
					if (object instanceof MagicCard) {
						cci++;
					} else if (object instanceof ICardCountable) {
						cci += ((ICardCountable) object).getCount();
					}
				}
			} else if (object instanceof CardGroup) {
				cci += ((CardGroup) object).getCreatureCount();
			}
		}
		setProperty(CREATURECOUNT_KEY, cci);
		return cci;
	}

	public synchronized int getOwnCount() {
		Integer iOwn = (Integer) getProperty(MagicCardFieldPhysical.OWN_COUNT.name());
		if (iOwn != null) {
			return iOwn.intValue();
		}
		int owncount = 0;
		for (Iterator<ICard> iterator = children.iterator(); iterator.hasNext();) {
			ICard object = iterator.next();
			if (object instanceof MagicCardPhysical) {
				if (((IMagicCardPhysical) object).isOwn()) {
					owncount += ((MagicCardPhysical) object).getCount();
				}
			} else if (object instanceof MagicCard) {
				owncount += ((MagicCard) object).getOwnCount();
			} else if (object instanceof CardGroup) {
				owncount += ((CardGroup) object).getOwnCount();
			}
		}
		setProperty(MagicCardFieldPhysical.OWN_COUNT.name(), owncount);
		return owncount;
	}

	public synchronized int getOwnUnique() {
		Integer iOwn = (Integer) getProperty(MagicCardFieldPhysical.OWN_UNIQUE.name());
		if (iOwn != null) {
			return iOwn.intValue();
		}
		int ownusize = 0;
		HashSet<IMagicCard> uniq = new HashSet<IMagicCard>();
		for (Iterator<ICard> iterator = children.iterator(); iterator.hasNext();) {
			ICard object = iterator.next();
			if (object instanceof MagicCardPhysical) {
				if (((IMagicCardPhysical) object).isOwn()) {
					uniq.add(((MagicCardPhysical) object).getBase());
				}
			} else if (object instanceof MagicCard) {
				Collection<MagicCardPhysical> physicalCards = ((MagicCard) object).getPhysicalCards();
				for (IMagicCardPhysical p : physicalCards) {
					if (p.isOwn()) {
						uniq.add((IMagicCard) object);
						break;
					}
				}
			} else if (object instanceof CardGroup) {
				ownusize += ((CardGroup) object).getOwnUnique();
			}
		}
		ownusize += uniq.size();
		setProperty(MagicCardFieldPhysical.OWN_UNIQUE.name(), ownusize);
		return ownusize;
	}

	public synchronized int getUniqueCount() {
		Integer ucount = (Integer) getProperty(MagicCardField.UNIQUE_COUNT.name());
		if (ucount != null) {
			return ucount.intValue();
		}
		int usize = 0;
		HashSet<IMagicCard> uniq = new HashSet<IMagicCard>();
		for (Iterator<ICard> iterator = children.iterator(); iterator.hasNext();) {
			ICard object = iterator.next();
			if (object instanceof MagicCardPhysical) {
				uniq.add(((MagicCardPhysical) object).getBase());
			} else if (object instanceof MagicCard) {
				uniq.add((IMagicCard) object);
			} else if (object instanceof CardGroup) {
				usize += ((CardGroup) object).getUniqueCount();
			}
		}
		usize += uniq.size();
		setProperty(MagicCardField.UNIQUE_COUNT.name(), usize);
		return usize;
	}

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

	public List getChildrenList() {
		return children;
	}

	public synchronized Object[] getChildren() {
		return getChildrenList().toArray(new Object[children.size()]);
	}

	public synchronized Iterator iterator() {
		return children.iterator();
	}

	public synchronized int size() {
		return this.children.size();
	}

	public synchronized void add(ICard elem) {
		this.children.add(elem);
		if (elem instanceof CardGroup) {
			CardGroup cardGroup = (CardGroup) elem;
			subs.put(cardGroup.getName(), (CardGroup) elem);
		}
		rehash();
	}

	public synchronized void remove(Object elem) {
		children.remove(elem);
		if (elem instanceof CardGroup) {
			CardGroup cardGroup = (CardGroup) elem;
			subs.remove(cardGroup.getName());
		}
		rehash();
	}

	/**
	 * @param index
	 */
	public synchronized Object getChildAtIndex(int index) {
		Object object = getChildrenList().get(index);
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
		Object value = getObjectByField(f);
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

	/**
	 * Get extra data object associated with the group, can be used for caching of card group
	 * properties
	 * 
	 * @return
	 */
	public Object getProperty(String key) {
		if (props == null)
			return null;
		return props.get(key);
	}

	public synchronized void setProperty(String key, Object value) {
		if (props == null) {
			props = new HashMap<String, Object>();
		}
		props.put(key, value);
	}

	public synchronized CardGroup getSubGroup(String key) {
		return subs.get(key);
	}

	public synchronized void clear() {
		children.clear();
		subs.clear();
		rehash();
	}

	public synchronized void rehash() {
		count = 0;
		props = null;
		base = null;
	}

	public synchronized IMagicCard getFirstCard() {
		if (children.size() == 0)
			return null;
		Object card = children.get(0);
		if (card instanceof CardGroup)
			return ((CardGroup) card).getFirstCard();
		if (card instanceof IMagicCard)
			return (IMagicCard) card;
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

	public static void expandGroups(Collection result, Collection cards) {
		for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
			Object o = iterator.next();
			if (o instanceof CardGroup)
				expandGroups(result, ((CardGroup) o).children);
			else
				result.add(o);
		}
	}

	public static String getGroupName(IMagicCard elem, ICardField field) {
		try {
			if (field == MagicCardField.COST) {
				return Colors.getColorName(elem.getCost());
			} else if (field == MagicCardField.CMC) {
				int ccc = elem.getCmc();
				if (ccc == 0 && elem.getType() != null && elem.getType().contains("Land")) {
					return "Land";
				} else {
					return String.valueOf(ccc);
				}
			} else if (field == MagicCardField.LANG) {
				if (elem.getLanguage() == null)
					return "English";
				else
					return elem.getLanguage();
			} else {
				return String.valueOf(elem.getObjectByField(field));
			}
		} catch (Exception e) {
			return "Unknown";
		}
	}

	public synchronized Collection<IMagicCard> expand() {
		ArrayList<IMagicCard> res = new ArrayList<IMagicCard>();
		expandGroups(res, children);
		return res;
	}

	public Object getObjectByField(ICardField field) {
		if (field == MagicCardField.NAME || field == groupField)
			return getName();
		if (size() == 0)
			return null;
		if (field == MagicCardFieldPhysical.OWN_COUNT)
			return getOwnCount();
		if (field == MagicCardFieldPhysical.OWN_UNIQUE)
			return getOwnUnique();
		return getBase().getObjectByField(field);
	}

	public ICard cloneCard() {
		throw new UnsupportedOperationException();
	}

	public void setLocation(Location location) {
		throw new UnsupportedOperationException();
	}

	public Location getLocation() {
		if (getBase() instanceof ILocatable)
			return ((ILocatable) getBase()).getLocation();
		return null;
	}
}
