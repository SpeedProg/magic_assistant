package com.reflexit.magiccards.core.model.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;

public abstract class AbstractMultiStore<T extends ICard> extends AbstractCardStore<T> implements
		ICardEventListener {
	protected HashMap<Location, AbstractCardStoreWithStorage<T>> map;
	protected Location defaultLocation;

	protected AbstractMultiStore() {
		init();
	}

	private void init() {
		this.map = new HashMap<Location, AbstractCardStoreWithStorage<T>>();
		this.defaultLocation = null;
	}

	@Override
	public void reload() {
		init();
		super.reload();
	}

	public ICardStore<T> getStore(Location location) {
		return map.get(location);
	}

	protected void addCardStore(AbstractCardStoreWithStorage<T> table) {
		this.map.put(table.getLocation(), table);
		table.addListener(this);
	}

	@Override
	public IStorage<T> getStorage() {
		return storageWrapper;
	}

	@Override
	protected synchronized boolean doAddAll(Collection<? extends T> col) {
		boolean commit = getStorage().isAutoCommit();
		getStorage().setAutoCommit(false);
		try {
			return super.doAddAll(col);
		} finally {
			getStorage().setAutoCommit(commit);
		}
	}

	@Override
	protected boolean doRemoveAll(Collection<? extends T> list) {
		boolean commit = getStorage().isAutoCommit();
		getStorage().setAutoCommit(true);
		try {
			return super.doRemoveAll(list);
		} finally {
			getStorage().setAutoCommit(commit);
		}
	}

	public AbstractCardStoreWithStorage getStorage(Location location) {
		AbstractCardStoreWithStorage loc = map.get(location);
		return loc;
	}

	public synchronized void removeLocation(Location location) {
		this.map.remove(location);
	}

	@Override
	public synchronized void doInitialize() {
		ArrayList<AbstractCardStoreWithStorage> all = new ArrayList<AbstractCardStoreWithStorage>();
		all.addAll(this.map.values());
		for (AbstractCardStoreWithStorage table : all) {
			Location oldLocation = table.getLocation();
			removeLocation(oldLocation);
			try {
				table.initialize();
				Location newLocation = table.getLocation();
				if (!newLocation.equals(oldLocation)) {
					MagicLogger.log("Key conflict - fixing: " + newLocation + " -> " + oldLocation);
					table.setLocation(oldLocation);
				}
			} catch (Exception e) {
				e.printStackTrace();
				MagicLogger.log(e);
			}
			addCardStore(table);
		}
	}

	@Override
	public Iterator<T> iterator() {
		final Iterator<AbstractCardStoreWithStorage<T>> iter = this.map.values().iterator();
		return new Iterator<T>() {
			Iterator<T> cur;
			{
				if (iter.hasNext())
					this.cur = (iter.next()).iterator();
				else
					this.cur = null;
			}

			@Override
			public boolean hasNext() {
				checkNext();
				return this.cur != null && this.cur.hasNext();
			}

			void checkNext() {
				if (this.cur == null)
					return;
				while (cur != null && !this.cur.hasNext()) {
					// if (!this.cur.hasNext()) {
					if (iter.hasNext()) {
						this.cur = (iter.next()).iterator();
					} else {
						this.cur = null;
					}
				}
			}

			@Override
			public T next() {
				checkNext();
				if (this.cur == null)
					throw new NoSuchElementException();
				return this.cur.next();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public int size() {
		// System.err.println(getDeepSize() + " " + size);
		return getDeepSize();
	}

	public synchronized int getDeepSize() {
		int s = 0;
		for (AbstractCardStoreWithStorage<T> table : map.values()) {
			s += table.size();
		}
		return s;
	}

	@Override
	public synchronized boolean doRemoveCard(final T card) {
		Location key = getLocation(card);
		AbstractCardStoreWithStorage res = this.map.get(key);
		if (res != null) {
			return res.doRemoveCard(card);
		}
		return false;
	}

	@Override
	public synchronized T doAddCard(final T card) {
		Location key = getLocation(card);
		AbstractCardStoreWithStorage<T> res = this.map.get(key);
		if (res == null) {
			res = newStorage(card);
			addCardStore(res);
		}
		return res.doAddCard(card);
	}

	@Override
	public void setMergeOnAdd(boolean v) {
		super.setMergeOnAdd(v);
		for (AbstractCardStoreWithStorage table : map.values()) {
			table.setMergeOnAdd(v);
		}
	}

	protected abstract AbstractCardStoreWithStorage newStorage(T card);

	protected abstract Location getLocation(final T card);

	/**
	 * @param location
	 */
	@Override
	public void setLocation(final Location location) {
		if (map.size() > 0) {
			if (map.get(location) == null)
				throw new IllegalArgumentException("key is invalid");
		}
		this.defaultLocation = location;
	}

	@Override
	public synchronized T getCard(int id) {
		for (AbstractCardStoreWithStorage<T> table : map.values()) {
			T card = table.getCard(id);
			if (card != null) {
				return card;
			}
		}
		return null;
	}

	@Override
	public synchronized Collection<T> getCards(int id) {
		ArrayList<T> arr = new ArrayList<T>();
		for (AbstractCardStoreWithStorage<T> table : map.values()) {
			Collection<T> cards = table.getCards(id);
			if (cards != null) {
				arr.addAll(cards);
			}
		}
		return arr;
	}

	@Override
	public void reindex() {
		for (AbstractCardStoreWithStorage<T> table : map.values()) {
			table.reindex();
		}
	}

	@Override
	public Location getLocation() {
		return defaultLocation;
	}

	public void renameLocation(Location oldLocation, Location newLocation) {
		AbstractCardStoreWithStorage loaded = map.get(oldLocation);
		if (loaded == null)
			loaded = map.get(newLocation);
		loaded.setLocation(newLocation);
		map.remove(oldLocation);
		map.put(newLocation, loaded);
	}

	@Override
	public boolean doRemoveAll() {
		boolean modified = false;
		for (AbstractCardStoreWithStorage table : map.values()) {
			if (table.removeAll()) {
				modified = true;
			}
		}
		return modified;
	}

	@Override
	public void handleEvent(CardEvent event) {
		// Propagate event from sub storages
		initialize();
		if (isListenerAttached())
			fireEvent(event);
	}

	IStorage<T> storageWrapper = new IStorage<T>() {
		boolean commit = true;

		@Override
		public boolean isAutoCommit() {
			return commit;
		}

		@Override
		public boolean isLoaded() {
			for (AbstractCardStoreWithStorage table : map.values()) {
				if (!table.getStorage().isLoaded())
					return false;
			}
			return true;
		}

		@Override
		public boolean isNeedToBeSaved() {
			for (AbstractCardStoreWithStorage table : map.values()) {
				if (table.getStorage().isNeedToBeSaved())
					return true;
			}
			return false;
		}

		@Override
		public void load() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void autoSave() {
			for (AbstractCardStoreWithStorage table : map.values()) {
				table.getStorage().autoSave();
			}
		}

		@Override
		public void save() {
			for (AbstractCardStoreWithStorage table : map.values()) {
				if (table.getStorage().isNeedToBeSaved())
					table.getStorage().save();
			}
		}

		@Override
		public void setAutoCommit(boolean value) {
			commit = value;
			for (AbstractCardStoreWithStorage table : map.values()) {
				table.getStorage().setAutoCommit(value);
			}
		}

		@Override
		public boolean add(T card) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(Collection<? extends T> list) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Iterator<T> iterator() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove(T o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll(Collection<? extends T> list) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll() {
			throw new UnsupportedOperationException();
		}

		@Override
		public int size() {
			return AbstractMultiStore.this.size();
		}

		@Override
		public Location getLocation() {
			return defaultLocation;
		}

		@Override
		public String getComment() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getName() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isVirtual() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setLocation(Location location) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean contains(T card) {
			throw new UnsupportedOperationException();
		}
	};

	@Override
	public boolean contains(T card) {
		Location loc = getLocation(card);
		AbstractCardStoreWithStorage<T> store = map.get(loc);
		return store.contains(card);
	}
}
