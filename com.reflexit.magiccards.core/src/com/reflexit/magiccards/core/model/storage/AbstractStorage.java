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
	protected boolean initialized;
	private boolean autocommit = true;

	/**
	 * 
	 */
	public AbstractStorage() {
		super();
	}

	public abstract void clear();

	public synchronized void initialize() {
		if (this.initialized)
			return;
		this.initialized = true;
		doInitialize();
	}

	protected abstract void doInitialize();

	public void save() {
		try {
			doSave();
		} catch (FileNotFoundException e) {
			throw new MagicException(e);
		}
	}

	public boolean addCard(T card) {
		doAddCard(card);
		autoSave();
		return true;
	}

	protected abstract void doSave() throws FileNotFoundException;

	public void addAll(Collection<T> list) throws MagicException {
		for (Object element : list) {
			T card = (T) element;
			doAddCard(card);
		}
		autoSave();
	}

	protected abstract boolean doAddCard(T card);

	public void setAutoCommit(boolean value) {
		this.autocommit = value;
	}

	public boolean removeCard(T card) {
		if (!doRemoveCard(card))
			return false;
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
	}
}