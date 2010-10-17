package com.reflexit.magiccards.core.model.storage;

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
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.Colors;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCardComparator;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.MagicCardFilter.BinaryExpr;
import com.reflexit.magiccards.core.model.MagicCardFilter.Expr;
import com.reflexit.magiccards.core.model.MagicCardFilter.Node;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;

/**
 * Class that implements IFilteredCardStore, it is only contains filtered
 * filteredList and no phisical media
 * 
 * @author Alena
 * 
 * @param <T>
 */
public abstract class AbstractFilteredCardStore<T> implements IFilteredCardStore<T> {
	private static final CardGroup[] EMPTY_CARD_GROUP = new CardGroup[0];
	protected Collection filteredList = null;
	protected Map<String, CardGroup> groupsList = new LinkedHashMap<String, CardGroup>();
	protected boolean initialized = false;
	protected MagicCardFilter filter;

	public MagicCardFilter getFilter() {
		return filter;
	}

	public void setFilter(MagicCardFilter filter) {
		this.filter = filter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.reflexit.magiccards.core.model.IFilteredCardStore#getSize()
	 */
	public int getSize() {
		initialize();
		return getFilteredList().size();
	}

	public Iterator<T> iterator() {
		return getFilteredList().iterator();
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

	public synchronized void update(MagicCardFilter filter) throws MagicException {
		setFilter(filter);
		update();
	}

	public synchronized void update() {
		initialize();
		if (filter == null)
			return;
		for (CardGroup g : groupsList.values()) {
			g.getChildren().clear();
			g.setCount(0);
		}
		setFilteredList(null);
		Collection filterCards = filterCards(this.filter);
		getFilteredList().addAll(filterCards);
	}

	public Collection<IMagicCard> filterCards(MagicCardFilter filter) throws MagicException {
		initialize();
		Collection<IMagicCard> filteredList;
		if (filter.getSortField() == null) {
			filteredList = new ArrayList<IMagicCard>();
			for (Iterator<IMagicCard> iterator = getCardStore().iterator(); iterator.hasNext();) {
				IMagicCard elem = iterator.next();
				if (!filter.isFiltered(elem)) {
					filteredList.add(elem);
				}
				if (filteredList.size() >= filter.getLimit()) {
					break;
				}
			}
		} else {
			Comparator<IMagicCard> comp = getSortComparator(filter);
			filteredList = new TreeSet<IMagicCard>(comp);
			for (Iterator<IMagicCard> iterator = getCardStore().iterator(); iterator.hasNext();) {
				IMagicCard elem = iterator.next();
				if (!filter.isFiltered(elem)) {
					filteredList.add(elem);
				}
				if (filteredList.size() > filter.getLimit()) {
					Object last = ((TreeSet) filteredList).last();
					filteredList.remove(last);
				}
			}
		}
		if (filter.getGroupField() != null) {
			if (groupsList.size() > 0) {
				CardGroup g = groupsList.values().iterator().next();
				if (g.getFieldIndex() != filter.getGroupField())
					groupsList.clear();
			}
			if (filter.getGroupField() == MagicCardField.TYPE) {
				CardGroup buildTypeGroups = CardStoreUtils.getInstance().buildTypeGroups(filteredList);
				for (Object o : buildTypeGroups.getChildren()) {
					if (o instanceof CardGroup) {
						CardGroup gr = (CardGroup) o;
						groupsList.put(gr.getName(), gr);
					}
				}
			} else {
				for (Object element : filteredList) {
					IMagicCard elem = (IMagicCard) element;
					CardGroup group = findGroupIndex(elem, filter.getGroupField());
					if (group != null) {
						group.add(elem);
					}
				}
			}
			for (Iterator iterator = groupsList.values().iterator(); iterator.hasNext();) {
				CardGroup g = (CardGroup) iterator.next();
				g.removeEmptyChildren();
				if (g.getChildren().size() == 0) {
					iterator.remove();
				}
			}
		}
		return filteredList;
	}

	protected Comparator<IMagicCard> getSortComparator(MagicCardFilter filter) {
		Comparator<IMagicCard> comp = MagicCardComparator.getComparator(filter.getSortField(), filter.isAscending());
		return comp;
	}

	/**
	 * @param elem
	 * @param cardField
	 * @return
	 */
	private CardGroup findGroupIndex(IMagicCard elem, ICardField cardField) {
		String name = null;
		try {
			if (cardField == MagicCardField.COST) {
				name = Colors.getColorName(elem.getCost());
			} else if (cardField == MagicCardField.CMC) {
				int ccc = elem.getCmc();
				if (ccc == 0 && elem.getType().contains("Land")) {
					name = "Land";
				} else {
					name = String.valueOf(ccc);
				}
			} else {
				name = String.valueOf(elem.getObjectByField(cardField));
			}
		} catch (Exception e) {
			name = "Unknown";
		}
		CardGroup g = this.groupsList.get(name);
		if (g == null && name != null) {
			g = new CardGroup(cardField, name);
			this.groupsList.put(name, g);
		}
		return g;
	}

	protected Collection<T> doCreateList() {
		return new ArrayList<T>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.reflexit.magiccards.core.model.IFilteredCardStore#getCardGroups()
	 */
	public CardGroup[] getCardGroups() {
		if (this.groupsList.size() == 0)
			return EMPTY_CARD_GROUP;
		return this.groupsList.values().toArray(new CardGroup[this.groupsList.size()]);
	}

	public CardGroup getCardGroup(int index) {
		return getCardGroups()[index];
	}

	public Location getLocation() {
		Expr root = getFilter().getRoot();
		Location loc = findLocationFilter(root);
		if (loc != null)
			return loc;
		return getCardStore().getLocation();
	}

	private Location findLocationFilter(Expr root) {
		if (root instanceof BinaryExpr) {
			BinaryExpr bin = ((BinaryExpr) root);
			if (bin.getLeft() instanceof Node && ((Node) bin.getLeft()).toString().equals(MagicCardFieldPhysical.LOCATION.name())) {
				return new Location(bin.getRight().toString());
			}
			Location loc = findLocationFilter(bin.getLeft());
			if (loc != null)
				return loc;
			loc = findLocationFilter(bin.getRight());
			if (loc != null)
				return loc;
		}
		return null;
	}

	public void setLocation(Location location) {
		throw new UnsupportedOperationException();
	}

	protected void reload() {
		initialized = false;
		initialize();
	}
}
