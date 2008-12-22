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

import java.io.FileNotFoundException;
import java.util.Collection;

import com.reflexit.magiccards.core.MagicException;

/**
 * @author Alena
 *
 */
public abstract class AbstractStorage<T> implements IStorage<T> {
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
		setLoaded(true);
		clearCache();
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
		boolean modified = doAddCard(card);
		if (modified) {
			setNeedToSave(true);
			autoSave();
		}
		return modified;
	}

	protected abstract void doSave() throws FileNotFoundException;

	public boolean addAll(Collection<? extends T> list) {
		boolean modified = false;
		for (T element : list) {
			if (doAddCard(element)) {
				modified = true;
				setNeedToSave(true);
			}
		}
		if (modified)
			autoSave();
		return modified;
	}

	public boolean removeAll(Collection<?> list) {
		boolean modified = false;
		for (Object element : list) {
			if (doRemoveCard((T) element)) {
				modified = true;
				setNeedToSave(true);
			}
		}
		if (modified)
			autoSave();
		return modified;
	}

	public boolean removeAll() {
		boolean modified = false;
		for (T element : this) {
			if (doRemoveCard(element)) {
				modified = true;
				setNeedToSave(true);
			}
		}
		autoSave();
		return modified;
	}

	protected abstract boolean doAddCard(T card);

	public void setAutoCommit(boolean value) {
		this.autocommit = value;
	}

	public boolean remove(T card) {
		if (!doRemoveCard(card))
			return false;
		setNeedToSave(true);
		autoSave();
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
}