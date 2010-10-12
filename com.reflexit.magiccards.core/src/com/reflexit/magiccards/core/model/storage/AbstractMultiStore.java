package com.reflexit.magiccards.core.model.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;

public abstract class AbstractMultiStore<T> extends AbstractCardStore<T> implements ILocatable, ICardEventListener,
        IStorageContainer<T> {
	protected HashMap<Location, AbstractCardStoreWithStorage<T>> map;
	protected int size;
	protected Location defaultLocation;

	public AbstractMultiStore() {
		this.map = new HashMap<Location, AbstractCardStoreWithStorage<T>>();
	}

	public ICardStore<T> getStore(Location location) {
		return map.get(location);
	}

	protected void addCardStore(AbstractCardStoreWithStorage<T> table) {
		this.map.put(table.getLocation(), table);
		this.size += table.size();
		table.addListener(this);
	}

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
	protected boolean doRemoveAll(Collection<?> list) {
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
		this.size = 0;
		ArrayList<AbstractCardStoreWithStorage> all = new ArrayList<AbstractCardStoreWithStorage>();
		all.addAll(this.map.values());
		for (AbstractCardStoreWithStorage table : all) {
			Location oldLocation = table.getLocation();
			removeLocation(oldLocation);
			try {
				table.initialize();
				Location newLocation = table.getLocation();
				if (!newLocation.equals(oldLocation)) {
					System.err.println("Key conflict - fixing: " + newLocation + " -> " + oldLocation);
					table.setLocation(oldLocation);
				}
				this.size += table.size();
			} catch (Exception e) {
				e.printStackTrace();
				Activator.log(e);
			}
			addCardStore(table);
		}
	}

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

			public boolean hasNext() {
				checkNext();
				return this.cur != null && this.cur.hasNext();
			}

			void checkNext() {
				if (this.cur == null)
					return;
				while (cur != null && !this.cur.hasNext()) {
					//if (!this.cur.hasNext()) {
					if (iter.hasNext()) {
						this.cur = (iter.next()).iterator();
					} else {
						this.cur = null;
					}
				}
			}

			public T next() {
				checkNext();
				if (this.cur == null)
					throw new NoSuchElementException();
				return this.cur.next();
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public int size() {
		//		getDeepSize();
		return this.size;
	}

	public int getDeepSize() {
		int s = 0;
		for (Object element : this) {
			s++;
		}
		if (size != s) {
			System.err.println("Size mismatch: " + s + " " + size);
			size = s;
		}
		return s;
	}

	@Override
	public synchronized boolean doRemoveCard(final T card) {
		Location key = getLocation(card);
		AbstractCardStoreWithStorage res = this.map.get(key);
		if (res != null) {
			int oldSize = res.size();
			boolean modified = res.doRemoveCard(card);
			this.size -= oldSize - res.size();
			return modified;
		}
		return false;
	}

	@Override
	public synchronized boolean doAddCard(final T card) {
		Location key = getLocation(card);
		AbstractCardStoreWithStorage res = this.map.get(key);
		if (res == null) {
			res = newStorage(card);
			addCardStore(res);
		}
		this.size -= res.size();
		boolean modified = res.doAddCard(card);
		this.size += res.size();
		return modified;
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
	public void setLocation(final Location location) {
		if (map.size() > 0) {
			if (map.get(location) == null)
				throw new IllegalArgumentException("key is invalid");
		}
		this.defaultLocation = location;
	}

	public IMagicCard getCard(int id) {
		Collection<IMagicCard> cards = getCards(id);
		if (cards.size() > 0) {
			return cards.iterator().next();
		}
		return null;
	}

	public Collection<IMagicCard> getCards(int id) {
		ArrayList<IMagicCard> arr = new ArrayList<IMagicCard>();
		for (AbstractCardStoreWithStorage table : map.values()) {
			if (table instanceof ICardCollection) {
				IMagicCard card = ((ICardCollection<IMagicCard>) table).getCard(id);
				if (card != null) {
					arr.add(card);
				}
			}
		}
		return arr;
	}

	public Location getLocation() {
		return defaultLocation;
	}

	public void renameLocation(Location oldLocation, Location newLocation) {
		AbstractCardStoreWithStorage loaded = map.get(oldLocation);
		if (loaded == null)
			loaded = map.get(newLocation);
		loaded.setLocation(newLocation);
		for (Iterator iterator = loaded.iterator(); iterator.hasNext();) {
			MagicCardPhisical card = (MagicCardPhisical) iterator.next();
			card.setLocation(newLocation);
		}
		loaded.setName(newLocation.getName());
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
		size = 0;
		return modified;
	}

	public void handleEvent(CardEvent event) {
		// Propagate event from sub storages
		doInitialize();
		fireEvent(event);
	}
	IStorage<T> storageWrapper = new IStorage<T>() {
		boolean commit = true;

		public boolean isAutoCommit() {
			return commit;
		}

		public boolean isLoaded() {
			for (AbstractCardStoreWithStorage table : map.values()) {
				if (!table.getStorage().isLoaded())
					return false;
			}
			return true;
		}

		public boolean isNeedToBeSaved() {
			for (AbstractCardStoreWithStorage table : map.values()) {
				if (table.getStorage().isNeedToBeSaved())
					return true;
			}
			return false;
		}

		public void load() {
			throw new UnsupportedOperationException();
		}
		public void autoSave() {
			for (AbstractCardStoreWithStorage table : map.values()) {
				table.getStorage().autoSave();
			}
		};
		public void save() {
			for (AbstractCardStoreWithStorage table : map.values()) {
				if (table.getStorage().isNeedToBeSaved())
					table.getStorage().save();
			}
		}

		public void setAutoCommit(boolean value) {
			commit = value;
			for (AbstractCardStoreWithStorage table : map.values()) {
				table.getStorage().setAutoCommit(value);
			}
			if (commit == true && isNeedToBeSaved()) {
				save();
			}
		}

		public boolean add(T card) {
			throw new UnsupportedOperationException();
		}

		public boolean addAll(Collection<? extends T> list) {
			throw new UnsupportedOperationException();
		}

		public Iterator<T> iterator() {
			throw new UnsupportedOperationException();
		}

		public boolean remove(T o) {
			throw new UnsupportedOperationException();
		}

		public boolean removeAll(Collection<?> list) {
			throw new UnsupportedOperationException();
		}

		public boolean removeAll() {
			throw new UnsupportedOperationException();
		}

		public int size() {
			return size;
		}

		public Location getLocation() {
			return defaultLocation;
		}

		public String getComment() {
			throw new UnsupportedOperationException();
		}

		public String getName() {
			throw new UnsupportedOperationException();
		}

		public boolean isVirtual() {
			throw new UnsupportedOperationException();
		}

		public void setLocation(Location location) {
			throw new UnsupportedOperationException();
		}


	};
}
