package com.reflexit.magiccards.core.model.storage;

import org.eclipse.core.commands.common.EventManager;

import java.util.Collection;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;

public abstract class AbstractCardStore<T> extends EventManager implements ICardStore<T> {
	protected transient boolean initialized = false;
	protected transient boolean autocommit = true;
	protected boolean mergeOnAdd = true;

	public void addListener(ICardEventListener lis) {
		addListenerObject(lis);
	}

	public void removeListener(ICardEventListener lis) {
		removeListenerObject(lis);
	}

	protected void fireEvent(CardEvent event) {
		Object[] listeners = getListeners();
		for (int i = 0; i < listeners.length; i++) {
			ICardEventListener lis = (ICardEventListener) listeners[i];
			try {
				lis.handleEvent(event);
			} catch (Throwable t) {
				Activator.log(t);
			}
		}
	}

	public final void initialize() {
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

	public void addAll(Collection<T> cards) {
		initialize();
		doAddAll(cards);
		if (this.autocommit)
			save();
		fireEvent(new CardEvent(this, CardEvent.ADD));
	}

	protected abstract void doAddAll(Collection cards);

	public boolean addCard(T card) {
		initialize();
		doAddCard(card);
		if (this.autocommit)
			save();
		fireEvent(new CardEvent(this, CardEvent.ADD));
		return true;
	}

	public void updateCard(T card) {
		initialize();
		if (this.autocommit)
			save();
		fireEvent(new CardEvent(card, CardEvent.UPDATE));
		return;
	}

	public void removeCard(T o) {
		initialize();
		doRemoveCard(o);
		if (this.autocommit)
			save();
		fireEvent(new CardEvent(this, CardEvent.REMOVE));
	}

	public void setAutoSave(boolean value) {
		this.autocommit = value;
	}

	public void setMergeOnAdd(boolean v) {
		this.mergeOnAdd = v;
	}

	public boolean getMergeOnAdd() {
		return this.mergeOnAdd;
	}

	protected abstract boolean doAddCard(T card);

	protected abstract void doRemoveCard(T card);
}
