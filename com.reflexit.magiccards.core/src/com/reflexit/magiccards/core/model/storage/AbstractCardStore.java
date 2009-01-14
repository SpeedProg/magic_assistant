package com.reflexit.magiccards.core.model.storage;

import java.util.Collection;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.MagicException;

public abstract class AbstractCardStore<T> implements ICardStore<T> {
	protected transient boolean initialized = false;
	protected boolean mergeOnAdd = true;

	public final void initialize() {
		if (isInitialized() == false) {
			try {
				doInitialize();
			} catch (final MagicException e) {
				Activator.log(e);
			} finally {
				setInitialized(true);
			}
		}
	}

	protected abstract void doInitialize() throws MagicException;

	public boolean addAll(final Collection<? extends T> cards) {
		initialize();
		return doAddAll(cards);
	}

	protected synchronized boolean doAddAll(final Collection<? extends T> col) {
		boolean modified = false;
		for (final Object element : col) {
			final T card = (T) element;
			if (doAddCard(card))
				modified = true;
		}
		return modified;
	}

	public boolean add(final T card) {
		initialize();
		synchronized (this) {
			if (!doAddCard(card))
				return false;
		}
		return true;
	}

	public boolean remove(final T o) {
		initialize();
		boolean res;
		synchronized (this) {
			res = doRemoveCard(o);
		}
		return res;
	}

	public boolean removeAll(Collection<?> list) {
		initialize();
		boolean modified = doRemoveAll(list);
		return modified;
	}

	public boolean removeAll() {
		initialize();
		boolean modified = doRemoveAll();
		return modified;
	}

	protected boolean doRemoveAll() {
		boolean modified = false;
		for (T t : this) {
			if (doRemoveCard(t))
				modified = true;
		}
		return modified;
	}

	protected boolean doRemoveAll(Collection<?> list) {
		boolean modified = false;
		for (Object t : list) {
			if (doRemoveCard((T) t))
				modified = true;
		}
		return modified;
	}

	public void setMergeOnAdd(final boolean v) {
		this.mergeOnAdd = v;
	}

	public boolean getMergeOnAdd() {
		return this.mergeOnAdd;
	}

	public void setInitialized(boolean b) {
		initialized = b;
	}

	public boolean isInitialized() {
		return initialized;
	}

	protected abstract boolean doAddCard(T card);

	protected abstract boolean doRemoveCard(T card);
}
