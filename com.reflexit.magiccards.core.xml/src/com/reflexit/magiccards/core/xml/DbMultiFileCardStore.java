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
package com.reflexit.magiccards.core.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.nav.MagicDbContainter;
import com.reflexit.magiccards.core.model.storage.AbstractCardStoreWithStorage;
import com.reflexit.magiccards.core.model.storage.AbstractMultiStore;
import com.reflexit.magiccards.core.model.storage.ICardCollection;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;
import com.reflexit.magiccards.core.model.utils.IntHashtable;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

/**
 * Card Store for Magic DB
 * 
 */
public class DbMultiFileCardStore extends AbstractMultiStore<IMagicCard> implements ICardCollection<IMagicCard>, IDbCardStore<IMagicCard> {
	private IntHashtable hash = new IntHashtable();
	private boolean load;

	public DbMultiFileCardStore() {
		super();
	}

	public DbMultiFileCardStore(boolean load) {
		this.load = load;
	}

	public synchronized DbFileCardStore addFile(final File file, final Location location, boolean initialize) {
		if (location != null && map.containsKey(location)) {
			return (DbFileCardStore) map.get(location);
		}
		DbFileCardStore store = new DbFileCardStore(file, location, hash, initialize);
		if (initialize) {
			store.initialize();
		}
		addCardStore(store);
		return store;
	}

	public int getCount() {
		return getStorage().size();
	}

	@Override
	protected AbstractCardStoreWithStorage<IMagicCard> newStorage(IMagicCard card) {
		DbFileCardStore store = new DbFileCardStore(getFile(card), getLocation(card), hash, false);
		store.getStorage().setAutoCommit(getStorage().isAutoCommit());
		return store;
	}

	@Override
	public void update(IMagicCard card) {
		if (card instanceof MagicCardPhysical)
			super.update(((MagicCardPhysical) card).getCard());
		else
			super.update(card);
	}

	@Override
	public IMagicCard getCard(int id) {
		return (IMagicCard) hash.get(id);
	}

	@Override
	public Collection<IMagicCard> getCards(int id) {
		System.err.println("getCards called");
		IMagicCard card = getCard(id);
		if (card == null)
			return Collections.EMPTY_LIST;
		ArrayList<IMagicCard> arr = new ArrayList<IMagicCard>(1);
		arr.add(card);
		return arr;
	}

	@Override
	protected boolean doUpdate(IMagicCard card) {
		getStorage(getLocation(card)).getStorage().autoSave();
		return super.doUpdate(card);
	}

	@Override
	protected Location getLocation(IMagicCard card) {
		return Location.fromCard(card);
	}

	@Override
	public Location getLocation() {
		return null;
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

	public File getFile(final IMagicCard card) {
		if (card instanceof MagicCard) {
			return new File(XmlCardHolder.getDbFolder(), Location.fromCard(card).getBaseFileName());
		} else
			throw new MagicException("Unknown card type");
	}

	@Override
	public synchronized void doInitialize() throws MagicException {
		MagicLogger.traceStart("db init");
		try {
			if (!load) {
				super.doInitialize();
				return;
			}
			this.load = false;
			ArrayList<File> files;
			// create initial database from flat file if not there
			new XmlCardHolder().loadInitialIfNot(ICoreProgressMonitor.NONE);
			// load card from xml in memory
			DbMultiFileCardStore table = this;
			if (!table.isInitialized()) {
				synchronized (table) {
					// System.err.println("Initializing DB");
					files = new ArrayList<File>();
					File[] members;
					try {
						MagicDbContainter con = DataManager.getModelRoot().getMagicDBContainer();
						members = con.getFile().listFiles();
						for (File file : members) {
							if (file.getName().endsWith(".xml"))
								files.add(file);
						}
					} catch (MagicException e) {
						MagicLogger.log(e);
						return;
					}
					// super.doInitialize();
					table.setInitialized(false);
					try {
						for (File file : files) {
							Location setLocation = Location.createLocation(file, Location.NO_WHERE);
							table.addFile(file, setLocation, true);
						}
					} finally {
						table.setInitialized(true);
					}
				}
			}
		} finally {
			MagicLogger.traceEnd("db init");
		}
	}

	public boolean isLoad() {
		return load;
	}

	public void setLoad(boolean load) {
		this.load = load;
	}

	@Override
	public List<IMagicCard> getCandidates(String name) {
		List cards = Collections.EMPTY_LIST;
		if (name == null)
			return cards;
		for (Iterator iterator = iterator(); iterator.hasNext();) {
			MagicCard a = (MagicCard) iterator.next();
			String lname = a.getName();
			if (name.equalsIgnoreCase(lname)) {
				if (cards == Collections.EMPTY_LIST) {
					cards = new ArrayList<IMagicCard>(2);
				}
				cards.add(a);
			}
		}
		return cards;
	}
}
