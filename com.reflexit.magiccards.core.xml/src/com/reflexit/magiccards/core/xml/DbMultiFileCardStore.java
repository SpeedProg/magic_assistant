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

import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.ICardModifiable;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.nav.MagicDbContainter;
import com.reflexit.magiccards.core.model.storage.AbstractCardStoreWithStorage;
import com.reflexit.magiccards.core.model.storage.AbstractMultiStore;
import com.reflexit.magiccards.core.model.storage.ICardCollection;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;

/**
 * Card Store for Magic DB
 * 
 */
public class DbMultiFileCardStore extends AbstractMultiStore<IMagicCard> implements ICardCollection<IMagicCard>, IDbCardStore<IMagicCard> {
	public class GlobalDbHandler {
		private TIntObjectHashMap<IMagicCard> hash = new TIntObjectHashMap<IMagicCard>();
		// map from name to latest card
		private HashMap<String, Object> primeMap = new HashMap<String, Object>();
		private Comparator comp = new Comparator<MagicCard>() {
			@Override
			public int compare(MagicCard arg0, MagicCard arg1) {
				int id1 = arg0.getCardId();
				int id2 = arg1.getCardId();
				if (id1 == id2)
					return 0;
				boolean en1 = arg0.getEnglishCardId() == 0;
				boolean en2 = arg1.getEnglishCardId() == 0;
				if (id2 > id1 && en1 == en2 || (en2 && !en1)) {
					return 1;
				}
				return -1;
			}
		};

		public boolean hashAndResolve(IMagicCard card) {
			boolean conflict = false;
			int id = card.getCardId();
			IMagicCard prev = hash.get(id);
			if (prev != null) {
				boolean delcur = conflictMerge(prev, card);
				hash.put(prev.getCardId(), prev); // rehash prev it could have
													// changed
				if (delcur) {
					conflict = true;
				} else {
					hash.put(card.getCardId(), card); // id could have changed
				}
			} else
				hash.put(id, card);
			// map for name
			Object sibCard = primeMap.get(card.getName());
			if (sibCard == null)
				primeMap.put(card.getName(), card);
			else {
				if (sibCard instanceof Collection) {
					((Collection) sibCard).add(card);
				} else {
					TreeSet<MagicCard> list = new TreeSet<MagicCard>(comp);
					list.add((MagicCard) sibCard);
					list.add((MagicCard) card);
					primeMap.put(card.getName(), list);
				}
			}
			return conflict;
		}

		public boolean conflictMerge(IMagicCard prev, IMagicCard card) {
			if (prev.equals(card)) {
				// merge
				((MagicCard) prev).setNonEmptyFromCard(card.getBase());
				return true;
			}
			int id = card.getCardId();
			// redo
			Integer old = (Integer) prev.get(MagicCardField.SIDE);
			Integer cur = (Integer) card.get(MagicCardField.SIDE);
			Object prevPart = prev.get(MagicCardField.PART);
			Object curPart = card.get(MagicCardField.PART);
			if (old == 0 && cur == 0) {
				if (prevPart != null)
					old = 1;
				if (curPart != null)
					cur = 1;
			}
			if (old == cur) {
				System.err.println("STORE DOUBLE: " + prev + " " + old + "[" + prevPart + "] -> new " + card + "[" + curPart + "] " + cur);
				return true;
			} else {
				if (old == 1) {
					((ICardModifiable) prev).set(MagicCardField.ID, -id);
					return false;
				} else if (cur == 1) {
					((ICardModifiable) card).set(MagicCardField.ID, -id);
					return false;
				}
			}
			return false;
		}

		public IMagicCard get(int id) {
			return hash.get(id);
		}

		public void remove(IMagicCard card) {
			hash.remove(card.getCardId());
		}

		public MagicCard getPrime(String name) {
			Object object = primeMap.get(name);
			if (object == null)
				return null;
			if (object instanceof MagicCard)
				return (MagicCard) object;
			return ((Collection<MagicCard>) object).iterator().next();
		}

		public Collection<IMagicCard> getCandidates(String name) {
			Object object = primeMap.get(name);
			if (object instanceof MagicCard) {
				ArrayList<IMagicCard> arr = new ArrayList<IMagicCard>(1);
				arr.add((IMagicCard) object);
				return arr;
			} else {
				return (Collection<IMagicCard>) object;
			}
		}
	}

	private boolean loadDefault;
	private GlobalDbHandler handler = new GlobalDbHandler();

	public DbMultiFileCardStore(boolean load) {
		this.loadDefault = load;
	}

	public synchronized DbFileCardStore addFile(final File file, final Location location, boolean initialize) {
		if (location != null && map.containsKey(location)) {
			return (DbFileCardStore) map.get(location);
		}
		DbFileCardStore store = new DbFileCardStore(file, location, handler, initialize);
		if (initialize) {
			store.initialize();
		}
		addCardStore(store);
		return store;
	}

	@Override
	public int getCount() {
		return getStorage().size();
	}

	@Override
	protected AbstractCardStoreWithStorage<IMagicCard> newStorage(IMagicCard card) {
		DbFileCardStore store = new DbFileCardStore(getFile(card), getLocation(card), handler, false);
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
		return handler.get(id);
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
		AbstractCardStoreWithStorage storage = getStorage(getLocation(card));
		if (storage == null) {
			storage = newStorage(card);
			addCardStore(storage);
		}
		storage.getStorage().autoSave();
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

	public File getFile(final IMagicCard card) {
		if (card instanceof MagicCard) {
			return new File(XmlCardHolder.getDbFolder(), Location.fromCard(card).getBaseFileName());
		} else
			throw new MagicException("Unknown card type");
	}

	private boolean flatDbLoaded = false;

	public void loadFromSoftware() throws MagicException {
		if (flatDbLoaded)
			return;
		flatDbLoaded = true;
		if (System.getProperty("set10e") != null) {
			try {
				DataManager.getCardHandler().loadFromFlatResource("10E.txt");
			} catch (IOException e) {
				// ignore
				MagicLogger.log("Cannot loadDefault 10E");
			}
		} else {
			setInitialized(true);
			try {
				Collection<String> editions = Editions.getInstance().getNames();
				for (String set : editions) {
					String abbr = (Editions.getInstance().getEditionByName(set).getBaseFileName());
					try {
						// long time = System.currentTimeMillis();
						File setFile = new File(XmlCardHolder.getDbFolder(), Location.createLocationFromSet(set).getBaseFileName());
						if (!setFile.exists() || setFile.length() == 0)
							DataManager.getCardHandler().loadFromFlatResource(abbr + ".txt");
						// long nowtime = System.currentTimeMillis() - time;
						// System.err.println("Loading " + abbr + " took " +
						// nowtime / 1000 + " s "
						// +
						// nowtime % 1000 + " ms");
					} catch (IOException e) {
						// ignore
						MagicLogger.log("Cannot loadDefault " + abbr);
					}
				}
			} finally {
				setInitialized(false);
			}
		}
	}

	@Override
	public synchronized void doInitialize() throws MagicException {
		if (isInitialized())
			return;
		MagicLogger.traceStart("db init");
		try {
			if (!loadDefault) {
				super.doInitialize();
				return;
			}
			this.loadDefault = false;
			// create initial database from flat file if not there
			loadFromSoftware();
			// loadDefault card from xml in memory
			// System.err.println("Initializing DB");
			ArrayList<File> files = new ArrayList<File>();
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
			setInitialized(false);
			try {
				for (File file : files) {
					Location setLocation = Location.createLocation(file, Location.NO_WHERE);
					addFile(file, setLocation, true);
				}
			} finally {
				setInitialized(true);
			}
		} finally {
			MagicLogger.traceEnd("db init");
		}
	}

	@Override
	public MagicCard getPrime(String name) {
		return handler.getPrime(name);
	}

	@Override
	public Collection<IMagicCard> getCandidates(String name) {
		if (name == null)
			return Collections.EMPTY_LIST;
		Collection<IMagicCard> xcards = handler.getCandidates(name);
		if (xcards == null)
			return Collections.EMPTY_LIST;
		return xcards;
	}

	@Override
	public synchronized boolean isInitialized() {
		return super.isInitialized();
	}
}
