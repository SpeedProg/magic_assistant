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
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author Alena
 * 
 */
public class CardGroup implements ICardCountable {
	private String name;
	private ICardField groupField;
	private int count;
	private int usize;
	private ArrayList children;
	private HashMap<String, Object> props;
	private IMagicCard base;

	public CardGroup(ICardField fieldIndex, String name) {
		this.groupField = fieldIndex;
		this.name = name;
		this.children = new ArrayList(2);
	}

	public IMagicCard getBase() {
		if (base == null) {
			if (children.size() == 1 && !(children.get(0) instanceof CardGroup))
				return getFirstCard();
			base = new MagicCardPhysical(new MagicCard(), null);
			base.getBase().setName(name);
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
		if (o instanceof MagicCard)
			allNonTransientFields = MagicCardField.allNonTransientFields();
		else
			allNonTransientFields = MagicCardFieldPhysical.allNonTransientFields();
		for (int i = 0; i < allNonTransientFields.length; i++) {
			ICardField field = allNonTransientFields[i];
			if (field == MagicCardField.NAME) {
				continue;
			}
			Object value = o.getObjectByField(field);
			Object mine = base.getObjectByField(field);
			Object newmine = null;
			if (mine == null) {
				newmine = value;
			} else if (mine.equals(value)) {
				// good
			} else {
				if (field.getType() == String.class) {
					newmine = "*";
				} else if (field == MagicCardField.DBPRICE || field == MagicCardFieldPhysical.PRICE) {
					Float fvalue = (Float) value;
					Float fmain = (Float) mine;
					if (o instanceof MagicCardPhysical) {
						// && ((MagicCardPhysical) o).isOwn()
						int count = ((ICardCountable) o).getCount();
						newmine = fmain + fvalue * count;
					}
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

	public int getCount() {
		if (count == 0)
			calculateCount();
		return this.count;
	}

	public int getUSize() {
		if (usize == 0)
			calculateUSize();
		return this.usize;
	}

	public int calculateCount() {
		count = 0;
		for (Iterator iterator = getChildren().iterator(); iterator.hasNext();) {
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

	public int calculateUSize() {
		usize = 0;
		for (Iterator iterator = getChildren().iterator(); iterator.hasNext();) {
			Object o = iterator.next();
			if (o instanceof CardGroup) {
				usize += ((CardGroup) o).calculateUSize();
			} else {
				usize++;
			}
		}
		return usize;
	}

	public ICardField getFieldIndex() {
		return this.groupField;
	}

	public Collection getChildren() {
		return this.children;
	}

	public int size() {
		return this.children.size();
	}

	public void add(Object elem) {
		this.children.add(elem);
		count = 0;
		base = null;
	}

	/**
	 * @param index
	 */
	public Object getChildAtIndex(int index) {
		return this.children.get(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.IMagicCard#getByIndex(int)
	 */
	public String getLabelByField(ICardField f) {
		if (f == this.groupField)
			return this.name;
		if (children.size() == 0)
			return "";
		Object value = getBase().getObjectByField(f);
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
			return name.equals(((CardGroup) arg0).name);
		}
		return false;
	}

	public void removeEmptyChildren() {
		for (Iterator iterator = getChildren().iterator(); iterator.hasNext();) {
			Object o = iterator.next();
			if (o instanceof CardGroup) {
				((CardGroup) o).removeEmptyChildren();
				if (((CardGroup) o).getChildren().size() == 0) {
					iterator.remove();
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

	public void setProperty(String key, Object value) {
		if (props == null) {
			props = new HashMap<String, Object>();
		}
		props.put(key, value);
	}

	public CardGroup getSubGroup(String key) {
		for (Iterator iterator = getChildren().iterator(); iterator.hasNext();) {
			Object o = iterator.next();
			if (o instanceof CardGroup) {
				if (((CardGroup) o).getName().equals(key))
					return (CardGroup) o;
			}
		}
		return null;
	}

	public void clear() {
		count = 0;
		props = null;
		children.clear();
	}

	public IMagicCard getFirstCard() {
		if (children.size() == 0)
			return null;
		Object card = children.get(0);
		if (card instanceof CardGroup)
			return ((CardGroup) card).getFirstCard();
		if (card instanceof IMagicCard)
			return (IMagicCard) card;
		return null;
	}

	public boolean contains(IMagicCard card) {
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
}
