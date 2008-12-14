package com.reflexit.magiccards.core.model.storage;

import java.util.Collection;

import org.eclipse.core.commands.common.EventManager;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;

public abstract class AbstractCardStore<T> extends EventManager implements ICardStore<T> {
	protected transient boolean initialized = false;
	protected boolean mergeOnAdd = true;

	public void addListener(final ICardEventListener lis) {
		addListenerObject(lis);
	}

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
				Activator.log(t);
			}
		}
	}

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

	public void addAll(final Collection<T> cards) {
		initialize();
		doAddAll(cards);
		fireEvent(new CardEvent(this, CardEvent.ADD, cards));
	}

	protected synchronized void doAddAll(final Collection<T> col) {
		for (final Object element : col) {
			final T card = (T) element;
			doAddCard(card);
		}
	}

	public boolean addCard(final T card) {
		initialize();
		synchronized (this) {
			if (!doAddCard(card))
				return false;
		}
		fireEvent(new CardEvent(this, CardEvent.ADD, card));
		return true;
	}

	public void updateCard(final T card) {
		initialize();
		fireEvent(new CardEvent(card, CardEvent.UPDATE, card));
		return;
	}

	public void removeCard(final T o) {
		initialize();
		boolean res;
		synchronized (this) {
			res = doRemoveCard(o);
		}
		if (res)
			fireEvent(new CardEvent(this, CardEvent.REMOVE, o));
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
