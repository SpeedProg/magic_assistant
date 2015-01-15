package com.reflexit.magiccards.core.model.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.EventManager;
import com.reflexit.magiccards.core.model.events.ICardEventListener;

public abstract class AbstractCardStore<T> extends EventManager implements ICardStore<T>, ILocatable {
	protected transient boolean initialized;
	protected boolean mergeOnAdd;

	{
		init();
	}

	private void init() {
		initialized = false;
		mergeOnAdd = true;
	}

	public void reload() {
		init();
	}

	@Override
	public final synchronized void initialize() {
		try {
			if (isInitialized() == false) {
				try {
					doInitialize();
				} catch (final MagicException e) {
					MagicLogger.log(e);
				} finally {
					setInitialized(true);
				}
			}
		} finally {
			this.notifyAll();
		}
	}

	@Override
	public void reindex() {
		// not indexed
	}

	protected abstract void doInitialize() throws MagicException;

	@Override
	public boolean addAll(final Collection<? extends T> cards) {
		initialize();
		boolean modified = doAddAll(cards);
		if (modified && isListenerAttached()) {
			fireEvent(new CardEvent(this, CardEvent.ADD, cards));
		}
		return modified;
	}

	public boolean isAutoCommit() {
		IStorage<T> storage = getStorage();
		return storage.isAutoCommit();
	}

	public void setAutoCommit(boolean commit) {
		IStorage<T> storage = getStorage();
		storage.setAutoCommit(commit);
		if (commit)
			storage.save();
	}

	protected synchronized boolean doAddAll(final Collection<? extends T> col) {
		boolean modified = false;
		boolean commit = isAutoCommit();
		setAutoCommit(false);
		try {
			for (final T element : col) {
				final T card = element;
				if (doAddCard(card))
					modified = true;
			}
		} finally {
			setAutoCommit(commit);
		}
		return modified;
	}

	@Override
	public boolean add(final T card) {
		initialize();
		synchronized (this) {
			if (!doAddCard(card))
				return false;
		}
		if (isListenerAttached())
			fireEvent(new CardEvent(this, CardEvent.ADD, card));
		return true;
	}

	@Override
	public boolean remove(final T o) {
		initialize();
		boolean res;
		synchronized (this) {
			res = doRemoveCard(o);
		}
		if (res && isListenerAttached())
			fireEvent(new CardEvent(this, CardEvent.REMOVE, o));
		return res;
	}

	@Override
	public boolean removeAll(Collection<? extends T> list) {
		initialize();
		boolean modified = doRemoveAll(list);
		if (modified && isListenerAttached()) {
			fireEvent(new CardEvent(this, CardEvent.REMOVE, list));
		}
		return modified;
	}

	@Override
	public boolean removeAll() {
		initialize();
		boolean modified = doRemoveAll();
		if (modified && isListenerAttached()) {
			fireEvent(new CardEvent(this, CardEvent.REMOVE, null));
		}
		return modified;
	}

	protected boolean doRemoveAll() {
		boolean modified = false;
		boolean commit = isAutoCommit();
		setAutoCommit(false);
		try {
			for (T t : this) {
				if (doRemoveCard(t))
					modified = true;
			}
			return modified;
		} finally {
			setAutoCommit(commit);
		}
	}

	protected boolean doRemoveAll(Collection<? extends T> list) {
		boolean modified = false;
		boolean commit = isAutoCommit();
		setAutoCommit(false);
		try {
			for (T t : list) {
				if (doRemoveCard(t))
					modified = true;
			}
			return modified;
		} finally {
			setAutoCommit(commit);
		}
	}

	@Override
	public void setMergeOnAdd(final boolean v) {
		this.mergeOnAdd = v;
	}

	@Override
	public boolean getMergeOnAdd() {
		return this.mergeOnAdd;
	}

	public void setInitialized(boolean b) {
		initialized = b;
	}

	public synchronized boolean isInitialized() {
		return initialized;
	}

	protected abstract boolean doAddCard(T card);

	protected abstract boolean doRemoveCard(T card);

	@Override
	public void addListener(final ICardEventListener lis) {
		addListenerObject(lis);
	}

	@Override
	public void removeListener(final ICardEventListener lis) {
		removeListenerObject(lis);
	}

	protected void fireEvent(final CardEvent event) {
		final Object[] listeners = getListeners();
		for (final Object listener : listeners) {
			final ICardEventListener lis = (ICardEventListener) listener;
			try {
				lis.handleEvent(event);
			} catch (final Throwable t) {
				MagicLogger.log(t);
			}
		}
	}

	@Override
	public void update(final T card, Set<? extends ICardField> mask) {
		initialize();
		synchronized (this) {
			if (!doUpdate(card, mask))
				return;
		}
		if (isListenerAttached())
			fireEvent(new CardEvent(card, CardEvent.UPDATE, mask));
		return;
	}

	protected boolean doUpdate(T card, Set<? extends ICardField> mask) {
		return true;
	}

	public int getUniqueCount() {
		return size();
	}

	@Override
	public void updateList(Collection<T> cards, Set<? extends ICardField> mask) {
		initialize();
		if (isListenerAttached())
			fireEvent(new CardEvent(this, CardEvent.UPDATE_LIST, cards));
		return;
	}

	@Override
	public Collection<T> getCards() {
		ArrayList<T> list = new ArrayList<T>();
		for (T t : this) {
			list.add(t);
		}
		return list;
	}
}
