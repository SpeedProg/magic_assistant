package com.reflexit.magiccards.core.model.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Editions.Edition;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.ICardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.MagicCardFilter.BinaryExpr;
import com.reflexit.magiccards.core.model.MagicCardFilter.Expr;
import com.reflexit.magiccards.core.model.MagicCardFilter.Node;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;

/**
 * Class that implements IFilteredCardStore, it is only contains filtered filteredList and no
 * physical media
 * 
 * @author Alena
 * 
 * @param <T>
 */
public abstract class AbstractFilteredCardStore<T> implements IFilteredCardStore<T> {
	private static final ICardGroup[] EMPTY_CARD_GROUP = new ICardGroup[0];
	protected Collection filteredList = null;
	protected final CardGroup rootGroup = new CardGroup(null, "Root");
	protected boolean initialized = false;
	protected MagicCardFilter filter = new MagicCardFilter();

	public MagicCardFilter getFilter() {
		return filter;
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
				MagicLogger.log(e);
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

	/**
	 * Count of the cards in the filtered list
	 */
	public int getCount() {
		int count = 0;
		synchronized (this) {
			for (T element : this) {
				if (element instanceof ICardCountable) {
					count += ((ICardCountable) element).getCount();
				} else {
					count++;
				}
			}
			return count;
		}
	}

	public boolean contains(T card) {
		synchronized (this) {
			for (T element : this) {
				if (element.equals(card))
					return true;
			}
			return false;
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

	public synchronized Collection<T> getFilteredList() {
		if (this.filteredList == null)
			this.filteredList = doCreateList();
		return this.filteredList;
	}

	public synchronized void update() {
		String key = "udpate " + getClass().getSimpleName();
		MagicLogger.traceStart(key);
		initialize();
		if (filter == null)
			return;
		rootGroup.clear();
		setFilteredList(null);
		Collection filterCards = filterCards();
		getFilteredList().addAll(filterCards);
		MagicLogger.traceEnd(key);
	}

	private Collection<IMagicCard> filterCards() throws MagicException {
		Collection<IMagicCard> filteredList = sortCards(filter);
		groupCards(filter, filteredList);
		return filteredList;
	}

	protected void groupCards(MagicCardFilter filter, Collection<IMagicCard> filteredList) {
		if (filter.getGroupField() != null) {
			rootGroup.clear(); // was already
			if (filter.getGroupField() == MagicCardField.TYPE) {
				ICardGroup buildTypeGroups = CardStoreUtils.buildTypeGroups(filteredList);
				for (Object gr : buildTypeGroups.getChildren()) {
					rootGroup.add((ICard) gr);
				}
			} else {
				for (Object element : filteredList) {
					IMagicCard elem = (IMagicCard) element;
					ICardGroup group = findGroupIndex(elem, filter);
					if (group != null) {
						addToNameGroup(elem, group);
					}
				}
			}
			removeEmptyGroups();
			rootGroup.sort(getSortComparator(filter));
		}
	}

	public void addToNameGroup(IMagicCard elem, ICardGroup group) {
		if (group.getFieldIndex() == MagicCardField.NAME) {
			group.add(elem);
		} else {
			String key = getEnglishName(elem);
			ICardGroup nameGroup = null;
			if (group instanceof CardGroup)
				nameGroup = ((CardGroup) group).getSubGroup(key);
			if (nameGroup != null) {
				nameGroup.add(elem);
			} else {
				nameGroup = new CardGroup(MagicCardField.NAME, key);
				nameGroup.add(elem);
				group.add(nameGroup);
			}
		}
	}

	public String getEnglishName(IMagicCard elem) {
		int enId = elem.getEnglishCardId();
		if (enId != 0) {
			Object card = getCardStore().getCard(enId);
			if (card instanceof IMagicCard) {
				return ((IMagicCard) card).getName();
			}
		}
		return elem.getName();
	}

	protected void removeEmptyGroups() {
		rootGroup.removeEmptyChildren();
	}

	protected Collection<IMagicCard> sortCards(MagicCardFilter filter) {
		String key = "sort" + hashCode();
		MagicLogger.traceStart(key);
		Collection<IMagicCard> filteredList;
		if (filter.getSortOrder().isEmpty()) {
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
			Comparator<ICard> comp = getSortComparator(filter);
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
		if (filter.isOnlyLastSet())
			filteredList = removeSetDuplicates(filteredList);
		MagicLogger.traceEnd(key);
		return filteredList;
	}

	protected Collection<IMagicCard> removeSetDuplicates(Collection<IMagicCard> filteredList) {
		LinkedHashMap<String, IMagicCard> unique = new LinkedHashMap<String, IMagicCard>();
		for (Iterator<IMagicCard> iterator = filteredList.iterator(); iterator.hasNext();) {
			IMagicCard elem = iterator.next();
			if (elem instanceof MagicCard) {
				MagicCard card = (MagicCard) elem;
				IMagicCard old = unique.get(card.getName());
				if (old == null) {
					unique.put(card.getName(), card);
				} else {
					Edition oldE = Editions.getInstance().getEditionByName(old.getSet());
					Edition newE = Editions.getInstance().getEditionByName(card.getSet());
					if (oldE != null && newE != null && oldE.getReleaseDate() != null && newE.getReleaseDate() != null) {
						if (oldE.getReleaseDate().before(newE.getReleaseDate())) {
							unique.put(card.getName(), card);
						}
						continue;
					}
					if (old.getCardId() < card.getCardId()) {
						unique.put(card.getName(), card);
					}
				}
			}
		}
		if (unique.size() > 0)
			return unique.values();
		return filteredList;
	}

	protected Comparator<ICard> getSortComparator(MagicCardFilter filter) {
		Comparator<ICard> comp = filter.getSortOrder().getComparator();
		return comp;
	}

	/**
	 * @param elem
	 * @param filter2
	 * @return
	 */
	private ICardGroup findGroupIndex(IMagicCard elem, MagicCardFilter filter) {
		ICardField[] groupFields = filter.getGroupFields();
		CardGroup parent = rootGroup;
		for (int i = 0; i < groupFields.length; i++) {
			ICardField field = groupFields[i];
			if (field == null)
				continue;
			String name = CardGroup.getGroupName(elem, field);
			if (name == null)
				return null;
			CardGroup g = parent.getSubGroup(name);
			if (g == null) {
				g = new CardGroup(field, name);
				parent.add(g);
			}
			parent = g;
		}
		return parent;
	}

	protected Collection<T> doCreateList() {
		return new ArrayList<T>();
	}

	public synchronized ICardGroup getCardGroupRoot() {
		return rootGroup;
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
				return Location.createLocation(bin.getRight().toString());
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

	@Override
	public String toString() {
		return filteredList.toString();
	}

	public void clear() {
		filteredList.clear();
	}

	public void addAll(ICardStore<T> store) {
		for (T object : store) {
			addFilteredCard(object);
		}
	}

	public int getUniqueCount() {
		return filteredList.size();
	}
}
