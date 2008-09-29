package com.reflexit.magiccards.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.MagicException;

/**
 * Class that implements IFilteredCardStore, it is only contains filtered filteredList
 * and no phisical media
 * @author Alena
 *
 * @param <T>
 */
public abstract class AbstractFilteredCardStore<T> implements IFilteredCardStore {
	protected Collection filteredList = null;
	protected Map<String, CardGroup> groupsList = new LinkedHashMap<String, CardGroup>();
	protected boolean initialized = false;

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.IFilteredCardStore#getSize()
	 */
	public int getSize() {
		initialize();
		return getFilteredList().size();
	}

	public T getCard(int index) {
		initialize();
		return doGetCard(index);
	}

	protected synchronized final void initialize() {
		if (this.initialized == false) {
			try {
				doInitialize();
			} catch (MagicException e) {
				Activator.log(e);
			} finally {
				this.initialized = true;
			}
		}
	}

	protected void doInitialize() throws MagicException {
	}

	protected T doGetCard(int index) {
		Collection<T> l = getFilteredList();
		if (l instanceof List) {
			return ((List<T>) getFilteredList()).get(index);
		} else {
			throw new UnsupportedOperationException(l.getClass() + " is not direct access type");
		}
	}

	protected synchronized void addFilteredCard(T card) {
		getFilteredList().add(card);
	}

	protected synchronized void removeFilteredCard(T card) {
		getFilteredList().remove(card);
	}

	public Object[] getElements() {
		initialize();
		return getFilteredList().toArray();
	}

	public Object getElement(int index) {
		return getCard(index);
	}

	protected void setFilteredList(Collection list) {
		this.filteredList = list;
	}

	protected synchronized Collection<T> getFilteredList() {
		if (this.filteredList == null)
			this.filteredList = doCreateList();
		return this.filteredList;
	}

	public void update(MagicCardFilter filter) throws MagicException {
		initialize();
		this.groupsList.clear();
		setFilteredList(null);
		Collection filterCards = filterCards(filter);
		getFilteredList().addAll(filterCards);
	}

	public Collection<IMagicCard> filterCards(MagicCardFilter filter) throws MagicException {
		initialize();
		Comparator<IMagicCard> comp = MagicCardComparator.getComparator(filter.getSortIndex(), filter.isAscending());
		TreeSet filteredList = new TreeSet<IMagicCard>(comp);
		for (Iterator<IMagicCard> iterator = getCardStore().cardsIterator(); iterator.hasNext();) {
			IMagicCard elem = iterator.next();
			if (!filter.isFiltered(elem)) {
				filteredList.add(elem);
				if (filter.getGroupIndex() >= 0) {
					CardGroup group = findGroupIndex(elem, filter.getGroupIndex());
					if (group != null)
						group.add(elem);
				}
			}
		}
		return filteredList;
	}

	/**
	 * @param elem
	 * @param groupIndex
	 * @return
	 */
	private CardGroup findGroupIndex(IMagicCard elem, int groupIndex) {
		String name = null;
		if (groupIndex == IMagicCard.INDEX_COST) {
			name = Colors.getColorName(elem.getCost());
		} else if (groupIndex == IMagicCard.INDEX_CMC) {
			int ccc = elem.getCmc();
			if (ccc == 0 && elem.getType().contains("Land")) {
				name = "Land";
			} else {
				name = String.valueOf(ccc);
			}
		}
		CardGroup g = this.groupsList.get(name);
		if (g == null && name != null) {
			g = new CardGroup(groupIndex, name);
			this.groupsList.put(name, g);
		}
		return g;
	}

	protected Collection<T> doCreateList() {
		return new ArrayList<T>();
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.IFilteredCardStore#getCardGroups()
	 */
	public Object[] getCardGroups() {
		if (this.groupsList.size() == 0)
			return null;
		return this.groupsList.values().toArray();
	}
}
