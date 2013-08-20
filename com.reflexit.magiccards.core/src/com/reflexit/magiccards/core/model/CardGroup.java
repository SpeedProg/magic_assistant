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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.reflexit.magiccards.core.model.MagicCardFilter.TextValue;
import com.reflexit.magiccards.core.model.storage.ILocatable;

/**
 * @author Alena
 * 
 */
public class CardGroup implements ICardCountable, ICard, ILocatable, IMagicCardPhysical, ICardGroup {
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

	public IMagicCard getBase() {
		throw new UnsupportedOperationException();
	}

	private synchronized IMagicCardPhysical getGroupBase() {
		if (base == null) {
			if (size() == 0)
				return null;
			for (Iterator iterator = children.iterator(); iterator.hasNext();) {
				Object o = iterator.next();
				if (o instanceof CardGroup) {
					CardGroup g = (CardGroup) o;
					if (g.size() > 0)
						addBase(g.getGroupBase());
				} else if (o instanceof IMagicCard) {
					addBase((IMagicCard) o);
				}
			}
		}
		return base;
	}

	private void createBase(IMagicCard card) {
		MagicCardPhysical base;
		if (card instanceof MagicCardPhysical) {
			base = (MagicCardPhysical) card.cloneCard();
			MagicCard refCard = (MagicCard) card.getBase().cloneCard();
			base.setMagicCard(refCard);
		} else if (card instanceof MagicCard) {
			MagicCard mc = (MagicCard) card;
			CardGroup realCards = mc.getRealCards();
			if (realCards == null)
				base = new MagicCardPhysical(card.cloneCard(), null);
			else {
				base = (MagicCardPhysical) realCards.getGroupBase().cloneCard();
				MagicCard refCard = mc.cloneCard();
				base.setMagicCard(refCard);
			}
		} else {
			throw new IllegalArgumentException();
		}
		base.getBase().setName(name);
		this.base = base;
	}

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

	private void addBase(IMagicCard o) {
		synchronized (this) {
			if (base == null) {
				createBase(o);
				return;
			}
		}
		ICardField[] allNonTransientFields = MagicCardFieldPhysical.allNonTransientFields();
		List<ICardField> list = new ArrayList<ICardField>(Arrays.asList(allNonTransientFields));
		list.remove(MagicCardField.ORACLE);
		list.remove(MagicCardField.NAME); // no need, processes separately
		list.add(MagicCardFieldPhysical.LOCATION); // need to add loctation because it is transient
		list.add(MagicCardField.ORACLE); // move to end, because want to set text first
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			ICardField field = (ICardField) iterator.next();
			Object value = o.getObjectByField(field);
			Object mine = getGroupBase().getObjectByField(field);
			Object newmine = null;
			if (mine == null) {
				newmine = value;
			} else {
				// Aggregate fields
				if (field == MagicCardField.DBPRICE || field == MagicCardFieldPhysical.PRICE || field == MagicCardField.RATING) {
					Float fvalue = (Float) value;
					Float fmain = (Float) mine;
					if (fvalue == null || fvalue.isNaN())
						fvalue = 0f;
					if (fmain.isNaN())
						fmain = 0f;
					if (o instanceof MagicCardPhysical && o.getCardId() != 0) {
						// && ((MagicCardPhysical) o).isOwn()
						int count = ((ICardCountable) o).getCount();
						newmine = fmain + fvalue * count;
					} else {
						newmine = fmain + fvalue;
					}
				} else if (field == MagicCardField.POWER || field == MagicCardField.TOUGHNESS) {
					Float fvalue = MagicCard.convertFloat((String) value);
					Float fmain = MagicCard.convertFloat((String) mine);
					if (fvalue.isNaN())
						fvalue = 0f;
					if (fmain.isNaN())
						fmain = 0f;
					if (o instanceof MagicCardPhysical && o.getCardId() != 0) {
						// && ((MagicCardPhysical) o).isOwn()
						int count = ((ICardCountable) o).getCount();
						newmine = fmain + fvalue * count;
					} else {
						newmine = fmain + fvalue;
					}
				} else if (field == MagicCardFieldPhysical.COUNT || field == MagicCardFieldPhysical.FORTRADECOUNT) {
					Integer fvalue = (Integer) value;
					Integer fmain = (Integer) mine;
					newmine = fmain + ((fvalue == null) ? 0 : fvalue);
				} else {
					// Join Fiels
					if (mine.equals(value)) {
						// good
					} else {
						if (field == MagicCardFieldPhysical.LOCATION) {
							newmine = Location.NO_WHERE;
						} else if (field == MagicCardFieldPhysical.OWNERSHIP) {
							newmine = "false";
						} else if (field == MagicCardField.ID) {
							newmine = 0;
						} else if (value != null && value.getClass() == String.class) {
							// string?
							if (mine.toString().length() == 0) {
								newmine = value;
							} else {
								// System.err.println("join " + mine + "<>" + value);
								newmine = "*";
							}
						}
					}
				}
			}
			if (newmine != null) {
				((ICardModifiable) getGroupBase()).setObjectByField(field, String.valueOf(newmine));
			}
		}
	}

	@Override
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
			if (object instanceof CardGroup) {
				cci += ((CardGroup) object).getCreatureCount();
			} else if (object instanceof IMagicCard) {
				if (((IMagicCard) object).getPower() != null) {
					if (object instanceof MagicCard) {
						cci++;
					} else if (object instanceof ICardCountable) {
						cci += ((ICardCountable) object).getCount();
					}
				}
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
			if (object instanceof IMagicCardPhysical) {
				owncount += ((IMagicCardPhysical) object).getOwnCount();
			}
		}
		setProperty(MagicCardFieldPhysical.OWN_COUNT.name(), owncount);
		return owncount;
	}

	@Override
	public synchronized int getOwnTotalAll() {
		return 0; // not supported
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
	 * @see
	 * com.reflexit.magiccards.core.model.ICardGroup#add(com.reflexit.magiccards.core.model.ICard)
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
		if (field == MagicCardField.UNIQUE_COUNT)
			return getUniqueCount();
		return getGroupBase().getObjectByField(field);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public IMagicCard cloneCard() {
		throw new UnsupportedOperationException();
	}

	public void setLocation(Location location) {
		throw new UnsupportedOperationException();
	}

	public Location getLocation() {
		if (size() == 0)
			return Location.NO_WHERE;
		return getGroupBase().getLocation();
	}

	public String getComment() {
		if (size() == 0)
			return null;
		return getGroupBase().getComment();
	}

	public boolean isOwn() {
		if (size() == 0)
			return false;
		return getOwnCount() > 0;
	}

	public int getForTrade() {
		if (size() == 0)
			return 0;
		return getGroupBase().getForTrade();
	}

	public String getSpecial() {
		if (size() == 0)
			return null;
		return getGroupBase().getSpecial();
	}

	public String getCost() {
		if (size() == 0)
			return null;
		return getGroupBase().getCost();
	}

	public boolean isSideboard() {
		if (size() == 0)
			return false;
		return getGroupBase().isSideboard();
	}

	public int getCardId() {
		if (size() == 0)
			return 0;
		return getGroupBase().getCardId();
	}

	public int getGathererId() {
		if (size() == 0)
			return 0;
		return getGroupBase().getGathererId();
	}

	public String getOracleText() {
		if (size() == 0)
			return null;
		return getGroupBase().getOracleText();
	}

	public String getRarity() {
		if (size() == 0)
			return null;
		return getGroupBase().getRarity();
	}

	public String getSet() {
		if (size() == 0)
			return null;
		return getGroupBase().getSet();
	}

	public String getType() {
		if (size() == 0)
			return null;
		return getGroupBase().getType();
	}

	public String getPower() {
		if (size() == 0)
			return null;
		return getGroupBase().getPower();
	}

	public String getToughness() {
		if (size() == 0)
			return null;
		return getGroupBase().getToughness();
	}

	public String getColorType() {
		if (size() == 0)
			return null;
		return getGroupBase().getColorType();
	}

	public int getCmc() {
		if (size() == 0)
			return 0;
		return getGroupBase().getCmc();
	}

	public float getDbPrice() {
		if (size() == 0)
			return 0f;
		return getGroupBase().getDbPrice();
	}

	public float getPrice() {
		if (size() == 0)
			return 0f;
		return getGroupBase().getPrice();
	}

	public float getCommunityRating() {
		if (size() == 0)
			return 0f;
		return getGroupBase().getCommunityRating();
	}

	public String getArtist() {
		if (size() == 0)
			return null;
		return getGroupBase().getArtist();
	}

	public String getRulings() {
		if (size() == 0)
			return null;
		return getGroupBase().getRulings();
	}

	public String getText() {
		if (size() == 0)
			return null;
		return getGroupBase().getText();
	}

	public String getLanguage() {
		if (size() == 0)
			return null;
		return getGroupBase().getLanguage();
	}

	public boolean matches(ICardField left, TextValue right) {
		return getGroupBase().matches(left, right);
	}

	public int getEnglishCardId() {
		if (size() == 0)
			return 0;
		return getGroupBase().getEnglishCardId();
	}

	public int getFlipId() {
		if (size() == 0)
			return 0;
		return getGroupBase().getFlipId();
	}

	public Collection getValues() {
		ArrayList list = new ArrayList();
		ICardField[] xfields = MagicCardFieldPhysical.allNonTransientFields();
		for (ICardField field : xfields) {
			list.add(getObjectByField(field));
		}
		return list;
	}

	public int getSide() {
		if (size() == 0)
			return 0;
		return getGroupBase().getSide();
	}

	public int getCollectorNumberId() {
		if (size() == 0)
			return 0;
		return getGroupBase().getCollectorNumberId();
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
}
