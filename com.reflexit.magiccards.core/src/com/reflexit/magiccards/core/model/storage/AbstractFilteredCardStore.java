package com.reflexit.magiccards.core.model.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFilter;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;
import com.reflexit.magiccards.core.model.expr.BinaryExpr;
import com.reflexit.magiccards.core.model.expr.Expr;
import com.reflexit.magiccards.core.model.expr.Node;
import com.reflexit.magiccards.core.model.nav.ModelRoot;
import com.reflexit.magiccards.core.model.utils.CardStoreUtils;

/**
 * Class that implements IFilteredCardStore, it is only contains filtered
 * filteredList and no physical media
 *
 * @author Alena
 *
 * @param <T>
 */
public class AbstractFilteredCardStore<T> implements IFilteredCardStore<T> {
	protected final CardGroup rootGroup = new CardGroup(null, "All");
	protected boolean initialized = false;
	protected MagicCardFilter filter = new MagicCardFilter();
	private MagicCardFilter lastUsedfilter = filter;
	private boolean storeChanged = true;
	private ICardEventListener cardStoreListener = new ICardEventListener() {
		@Override
		public void handleEvent(CardEvent event) {
			setRefreshRequired(true);
		}
	};
	protected ICardStore<T> store;

	public AbstractFilteredCardStore(ICardStore<T> store) {
		this.store = store;
	}

	@Override
	public final ICardStore<T> getCardStore() {
		return store;
	};

	@Override
	public final MagicCardFilter getFilter() {
		return filter;
	}

	@Override
	public int getSize() {
		initialize();
		return rootGroup.getChildren().length;
	}

	@Override
	public int getFlatSize() {
		initialize();
		return rootGroup.getInt(MagicCardField.SIZE);
	}

	@Override
	public Iterator<T> iterator() {
		return (Iterator<T>) rootGroup.iterator();
	}

	public T getCard(int index) {
		initialize();
		return doGetCard(index);
	}

	protected synchronized final void initialize() {
		try {
			if (this.initialized == false) {
				try {
					doInitialize();
				} catch (MagicException e) {
					MagicLogger.log(e);
				} finally {
					this.initialized = true;
				}
			}
		} finally {
			this.notifyAll();
		}
	}

	protected void doInitialize() throws MagicException {
		storeChanged = true; // force update
		if (getCardStore() == null) {
			MagicLogger.log("Cannot initialize " + this + ". Store is null");
		} else
			getCardStore().initialize();
	}

	public boolean isInitialized() {
		return initialized;
	}

	private void reinstallListener() {
		getCardStore().removeListener(cardStoreListener);
		getCardStore().addListener(cardStoreListener);
	}

	@Override
	protected void finalize() throws Throwable {
		if (getCardStore() != null)
			getCardStore().removeListener(cardStoreListener);
		super.finalize();
	}

	protected T doGetCard(int index) {
		return (T) rootGroup.getChildAtIndex(index);
	}

	/**
	 * Count of the cards in the filtered list
	 */
	@Override
	public int getCount() {
		return rootGroup.getCount();
	}

	@Override
	public boolean contains(T card) {
		synchronized (this) {
			for (T element : this) {
				if (element.equals(card))
					return true;
			}
			return false;
		}
	}

	@Override
	public Object[] getElements() {
		initialize();
		return rootGroup.getChildren();
	}

	@Override
	public Object getElement(int index) {
		return getCard(index);
	}

	@Override
	public synchronized void update() {
		initialize();
		reinstallListener();
		if (filter == null)
			return;
		String key = "udpate " + getClass().getSimpleName();
		boolean filterChanged = !filter.equals(lastUsedfilter);
		MagicLogger.trace("storeupdate storeChanged=" + storeChanged + " filterChanged=" + filterChanged + " loc="
				+ getLocation());
		// new Exception().printStackTrace();
		boolean nonEmpty = rootGroup.size() > 0;
		if (storeChanged == false && filterChanged == false && nonEmpty) {
			MagicLogger.trace("skipped " + storeChanged + " " + filterChanged);
			return;
		}
		MagicLogger.traceStart(key);
		try {
			boolean groupEquals = filter.equalsGroups(lastUsedfilter);
			if (!storeChanged && groupEquals && nonEmpty) {
				MagicLogger.trace("re-filter");
				rootGroup.setFilter(filter);
			} else {
				MagicLogger.trace("re-group");
				groupCards(filter, getCardStore());
			}
		} finally {
			this.lastUsedfilter = (MagicCardFilter) filter.clone();
			this.storeChanged = false;
			MagicLogger.traceEnd(key);
		}
	}

	public void setRefreshRequired(boolean b) {
		this.storeChanged = b;
	}

	public boolean isRefreshRequired() {
		return this.storeChanged;
	}

	protected void groupCards(MagicCardFilter filter, Iterable<?> filteredList) {
		synchronized (filteredList) {
			rootGroup.clear();
			if (filter.getGroupField() != null) {
				if (filter.getGroupField() == MagicCardField.TYPE) {
					ICardGroup buildTypeGroups = CardStoreUtils.buildTypeGroups(filteredList);
					for (Object gr : buildTypeGroups.getChildren()) {
						rootGroup.add((ICard) gr);
					}
				} else {
					for (Object element : filteredList) {
						IMagicCard elem = (IMagicCard) element;
						ICardGroup group = findGroupIndex(elem, filter);
						addToNameGroup(elem, group);
					}
					if (filter.getGroupField() != MagicCardField.NAME) {
						removeSingleNameGroups(rootGroup);
					}
				}
			} else {
				rootGroup.addAll(filteredList);
			}
			rootGroup.setFilter(filter);
		}
	}

	private void removeSingleNameGroups(CardGroup group) {
		if (!filter.isNameGroupping())
			return;
		if (group.getFieldIndex() == MagicCardField.NAME) {
			if (group.size() == 1) {
				group.getParent().remove(group);
				group.getParent().add(group.getChildAtIndex(0));
			}
		} else {
			Collection<CardGroup> subGroups = new ArrayList<CardGroup>(group.getSubGroups());
			for (CardGroup sub : subGroups) {
				removeSingleNameGroups(sub);
			}
		}
	}

	public void addToNameGroup(IMagicCard elem, ICardGroup group) {
		if (group.getFieldIndex() == MagicCardField.NAME || !filter.isNameGroupping()) {
			group.add(elem);
		} else {
			String key = elem.getEnglishName();
			ICardGroup nameGroup = group.getSubGroup(key);
			if (nameGroup != null) {
				nameGroup.add(elem);
			} else {
				nameGroup = new CardGroup(MagicCardField.NAME, key);
				nameGroup.add(elem);
				group.add(nameGroup);
			}
		}
	}

	/**
	 * @param elem
	 * @param filter2
	 * @return
	 */
	private ICardGroup findGroupIndex(IMagicCard elem, MagicCardFilter filter) {
		ICardField[] groupFields = filter.getGroupOrder().getFields();
		CardGroup parent = rootGroup;
		for (int i = 0; i < groupFields.length; i++) {
			ICardField field = groupFields[i];
			if (field == null)
				continue;
			String name = CardGroup.getGroupName(elem, field);
			if (name == null)
				name = "null";
			CardGroup g = parent.getSubGroup(name);
			if (g == null) {
				g = new CardGroup(field, name);
				parent.add(g);
			}
			parent = g;
		}
		return parent;
	}

	@Override
	public ICardGroup getCardGroupRoot() {
		return rootGroup;
	}

	@Override
	public Location getLocation() {
		Expr root = getFilter().getRoot();
		Location loc = findLocationFilter(root);
		if (loc != null)
			return loc;
		return getCardStore().getLocation();
	}

	public ModelRoot getModelRoot() {
		return DataManager.getInstance().getModelRoot();
	}

	private Location findLocationFilter(Expr root) {
		if (root instanceof BinaryExpr) {
			BinaryExpr bin = ((BinaryExpr) root);
			if (bin.getLeft() instanceof Node
					&& ((Node) bin.getLeft()).toString().equals(MagicCardField.LOCATION.name())) {
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

	@Override
	public void setLocation(Location location) {
		throw new UnsupportedOperationException();
	}

	public void reload() {
		initialized = false;
		initialize();
	}

	@Override
	public String toString() {
		return rootGroup.toString();
	}

	@Override
	public void clear() {
		rootGroup.clear();
	}

	@Override
	public int getUniqueCount() {
		return rootGroup.getUniqueCount();
	}
}
