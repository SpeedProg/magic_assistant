/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.core.model.storage;

import org.eclipse.core.commands.common.EventManager;

import java.io.FileNotFoundException;
import java.util.Collection;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.events.CardEvent;
import com.reflexit.magiccards.core.model.events.ICardEventListener;

/**
 * @author Alena
 *
 */
public abstract class AbstractStorage<T> extends EventManager implements IStorage<T> {
	private boolean loaded = false;
	private boolean autocommit = true;
	private boolean needToSave = false;

	/**
	 * 
	 */
	public AbstractStorage() {
		super();
	}

	public synchronized void load() {
		if (isLoaded())
			return;
		clearCache();
		setLoaded(true);
		doLoad();
	}

	public synchronized void reload() {
		setLoaded(false);
		load();
	};

	public synchronized void unload() {
		setLoaded(false);
		clearCache();
	};

	public abstract void clearCache();

	protected abstract void doLoad();

	public synchronized void save() {
		try {
			doSave();
			setNeedToSave(false);
		} catch (FileNotFoundException e) {
			throw new MagicException(e);
		}
	}

	public boolean add(T card) {
		load();
		boolean modified;
		synchronized (this) {
			modified = doAddCard(card);
			if (modified) {
				setNeedToSave(true);
				autoSave();
			}
		}
		if (modified) {
			fireEvent(new CardEvent(this, CardEvent.ADD, card));
		}
		return modified;
	}

	protected abstract void doSave() throws FileNotFoundException;

	public boolean addAll(Collection<? extends T> list) {
		load();
		boolean modified = false;
		synchronized (this) {
			for (T element : list) {
				if (doAddCard(element)) {
					modified = true;
					setNeedToSave(true);
				}
			}
			if (modified)
				autoSave();
		}
		if (modified) {
			fireEvent(new CardEvent(this, CardEvent.ADD, list));
		}
		return modified;
	}

	public boolean removeAll(Collection<?> list) {
		load();
		boolean modified = false;
		synchronized (this) {
			for (Object element : list) {
				if (doRemoveCard((T) element)) {
					modified = true;
					setNeedToSave(true);
				}
			}
			if (modified)
				autoSave();
		}
		if (modified) {
			fireEvent(new CardEvent(this, CardEvent.REMOVE, list));
		}
		return modified;
	}

	public boolean removeAll() {
		load();
		boolean modified = false;
		synchronized (this) {
			for (T element : this) {
				if (doRemoveCard(element)) {
					modified = true;
					setNeedToSave(true);
				}
			}
			autoSave();
		}
		if (modified) {
			fireEvent(new CardEvent(this, CardEvent.REMOVE, null));
		}
		return modified;
	}

	protected abstract boolean doAddCard(T card);

	public void setAutoCommit(boolean value) {
		this.autocommit = value;
	}

	public boolean remove(T card) {
		load();
		synchronized (this) {
			if (!doRemoveCard(card))
				return false;
			setNeedToSave(true);
			autoSave();
		}
		fireEvent(new CardEvent(this, CardEvent.REMOVE, card));
		return true;
	}

	protected abstract boolean doRemoveCard(T card);

	public boolean isAutoCommit() {
		return this.autocommit;
	}

	public void autoSave() {
		if (isAutoCommit())
			save();
		else
			setNeedToSave(true);
	}

	public boolean isNeedToBeSaved() {
		return needToSave;
	}

	protected void setNeedToSave(boolean value) {
		needToSave = value;
	}

	protected void setLoaded(boolean value) {
		this.loaded = value;
	}

	public boolean isLoaded() {
		return loaded;
	}

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

	public void update(final T card) {
		load();
		synchronized (this) {
			if (!doUpdate(card))
				return;
			autoSave();
		}
		fireEvent(new CardEvent(card, CardEvent.UPDATE, card));
		return;
	}

	protected boolean doUpdate(T card) {
		return true;
	}
}