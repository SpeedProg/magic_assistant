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
package com.reflexit.magiccards.core;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICardHandler;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.IMagicCardPhysical;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.Predicate;
import com.reflexit.magiccards.core.model.nav.ModelRoot;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;
import com.reflexit.magiccards.core.model.storage.IDbPriceStore;
import com.reflexit.magiccards.core.model.storage.ILocatable;

public class DataManager {
	public static final String ID = "com.reflexit.magiccards.core";
	public static DataManager instance;
	private ICardHandler handler;
	private ModelRoot root;
	private TIntObjectHashMap<IMagicCard> links = new TIntObjectHashMap<IMagicCard>();
	private boolean owncopy;

	protected DataManager() {
		instance = this;
		try {
			// String variant1 =
			// "com.reflexit.magiccards.core.sql.handlers.CardHolder";
			String variant2 = "com.reflexit.magiccards.core.xml.XmlCardHolder";
			@SuppressWarnings("rawtypes")
			Class c = Class.forName(variant2);
			Object x = c.newInstance();
			handler = (ICardHandler) x;
		} catch (InstantiationException e) {
			MagicLogger.log(e);
		} catch (IllegalAccessException e) {
			MagicLogger.log(e);
		} catch (ClassNotFoundException e) {
			MagicLogger.log(e);
		}
	}

	public static DataManager getInstance() {
		if (instance == null)
			instance = new DataManager();
		return instance;
	}

	public synchronized static ICardHandler getCardHandler() {
		return getInstance().handler;
	}

	public static ICardStore getLibraryCardStore() {
		return getCardHandler().getLibraryCardStore();
	}

	public static IDbCardStore<IMagicCard> getMagicDBStore() {
		return getCardHandler().getMagicDBStore();
	}

	public static IDbPriceStore getDBPriceStore() {
		return getCardHandler().getDBPriceStore();
	}

	public static synchronized ModelRoot getModelRoot() {
		if (getInstance().root == null) {
			getInstance().root = ModelRoot.getInstance(new File(FileUtils.getWorkspaceFile(), "magiccards"));
		}
		return getInstance().root;
	}

	public ICardStore<IMagicCard> getCardStore(Location to) {
		return getCardHandler().getCardStore(to);
	}

	public void reset(File dir) {
		// File locDir = new File(FileUtils.getWorkspaceFile(), "magiccards");
		root = ModelRoot.getInstance(dir);
	}

	public void reset() {
		FileUtils.deleteTree(getRootDir());
		root = null;
		getModelRoot();
	}

	public static File getRootDir() {
		File rootDir = getModelRoot().getRootDir();
		if (rootDir == null)
			throw new NullPointerException();
		return rootDir;
	}

	boolean copyCards(Collection cards1, ICardStore<IMagicCard> store, Location to) {
		if (store == null)
			throw new NullPointerException();
		if (to == null)
			to = store.getLocation();
		ArrayList<IMagicCard> cards = new ArrayList<IMagicCard>(cards1.size());
		CardGroup.expandGroups(cards, cards1);
		boolean virtual = store.isVirtual();
		ArrayList<IMagicCard> list = new ArrayList<IMagicCard>(cards.size());
		boolean ownCopyAllowed = owncopy;
		for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			if (ownCopyAllowed == false && card instanceof MagicCardPhysical && virtual == false &&
					((MagicCardPhysical) card).isOwn()) {
				throw new MagicException(
						"Cannot copy own cards into non-virtual deck, use move instead - or override this protection in preferences");
			}
			// copied cards will have collection ownership for virtual
			MagicCardPhysical phi = new MagicCardPhysical(card, to, virtual);
			list.add(phi);
		}
		return add(store, list);
	}

	public boolean copyCards(Collection cards1, Location to) {
		ICardStore<IMagicCard> store = getCardStore(to);
		if (store == null)
			return false;
		return copyCards(cards1, store, to);
	}

	/**
	 * Using card representation create proper link to base or find actuall base card to replace fake one
	 * 
	 * @param input
	 * @return
	 */
	public Collection<IMagicCard> resolve(Collection<IMagicCard> input) {
		return resolve(input, new ArrayList<IMagicCard>(input.size()), getMagicDBStore());
	}

	private Collection<IMagicCard> resolve(Collection<IMagicCard> input, Collection<IMagicCard> output, ICardStore db) {
		for (Iterator iterator = input.iterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			if (card instanceof CardGroup) {
				resolve((Collection<IMagicCard>) ((CardGroup) card).getChildrenList(), output, db);
			} else {
				// Need to repair references to MagicCard instances
				IMagicCard cardRes = resolve(card, db);
				if (cardRes != null)
					output.add(cardRes);
			}
		}
		return output;
	}

	private IMagicCard resolve(IMagicCard card, ICardStore db) {
		if (card instanceof MagicCard) {
			card = (IMagicCard) db.getCard(card.getCardId());
			return card;
		} else if (card instanceof MagicCardPhysical) {
			IMagicCard base = (IMagicCard) db.getCard(card.getCardId());
			if (base != null) {
				((MagicCardPhysical) card).setMagicCard((MagicCard) base);
			} else {
				return null;
			}
		}
		return card;
	}

	public boolean moveCards(Collection cards1, Location to) {
		ICardStore<IMagicCard> sto = getCardStore(to);
		if (sto == null)
			return false;
		return moveCards(cards1, sto, to);
	}

	boolean moveCards(Collection cards1, ICardStore<IMagicCard> store, Location to) {
		if (store == null)
			throw new NullPointerException();
		if (to == null)
			to = store.getLocation();
		ArrayList<IMagicCard> cards = new ArrayList<IMagicCard>(cards1.size());
		CardGroup.expandGroups(cards, cards1);
		boolean virtual = store.isVirtual();
		ArrayList<IMagicCard> list = new ArrayList<IMagicCard>(cards.size());
		for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			MagicCardPhysical phi = new MagicCardPhysical(card, to, virtual);
			if (card instanceof MagicCardPhysical) {
				if (((IMagicCardPhysical) card).isOwn()) {
					if (virtual)
						throw new MagicException("Cannot move own cards to virtual collection. Use copy instead.");
					phi.setOwn(true);
				} else {
					phi.setOwn(false);
				}
			}
			list.add(phi);
		}
		boolean res = add(store, list);
		if (res) {
			boolean allthesame = true;
			Location from = null;
			for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
				IMagicCard card = (IMagicCard) iterator.next();
				if (!(card instanceof MagicCardPhysical))
					break;
				Location from2 = ((MagicCardPhysical) card).getLocation();
				if (from2 != null) {
					if (from == null)
						from = from2;
					else if (!from.equals(from2)) {
						allthesame = false;
						break;
					}
				}
			}
			if (from != null && allthesame) {
				ICardStore<IMagicCard> sfrom2 = getCardStore(from);
				if (sfrom2 != null) {
					remove(sfrom2, cards);
				}
				if (from.equals(to)) {
					reconcile(); // update all links too screwy to calculate
				}
			} else {
				for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
					IMagicCard card = (IMagicCard) iterator.next();
					if (!(card instanceof MagicCardPhysical))
						continue;
					MagicCardPhysical mcp = (MagicCardPhysical) card;
					remove(mcp);
				}
			}
		}
		return res;
	}

	public List<IMagicCard> splitCards(Collection<IMagicCard> cards1, int count) {
		ArrayList<IMagicCard> cards = new ArrayList<IMagicCard>(cards1.size());
		CardGroup.expandGroups(cards, cards1);
		List<IMagicCard> x = new ArrayList<IMagicCard>();
		for (IMagicCard o : cards) {
			if (o instanceof MagicCardPhysical) {
				MagicCardPhysical mcp = (MagicCardPhysical) o;
				MagicCardPhysical toMove = split(mcp, count);
				if (toMove != null)
					x.add(toMove);
			} else if (o instanceof MagicCard) {
				for (int i = 0; i < count; i++) {
					x.add(o);
				}
			}
		}
		return x;
	}

	public List<IMagicCard> splitCards(Map<IMagicCard, Integer> countMap) {
		List<IMagicCard> x = new ArrayList<IMagicCard>();
		for (IMagicCard o : countMap.keySet()) {
			int count = countMap.get(o);
			if (o instanceof MagicCardPhysical) {
				MagicCardPhysical mcp = (MagicCardPhysical) o;
				MagicCardPhysical toMove = split(mcp, count);
				if (toMove != null)
					x.add(toMove);
			} else if (o instanceof MagicCard) {
				for (int i = 0; i < count; i++) {
					x.add(o);
				}
			}
		}
		return x;
	}

	public boolean add(ICardStore store, Collection list) {
		boolean res = store.addAll(list);
		reconcile(list);
		return res;
	}

	public void remove(ICardStore store, Collection list) {
		store.removeAll(list);
		reconcile(list);
	}

	public void remove(MagicCardPhysical mcp) {
		Location from = mcp.getLocation();
		ICardStore<IMagicCard> store = getCardStore(from);
		if (store == null)
			throw new IllegalArgumentException("Cannot find store for " + from);
		store.remove(mcp);
		reconcile(mcp);
	}

	public boolean add(MagicCardPhysical mcp) {
		Location loc = mcp.getLocation();
		ICardStore<IMagicCard> store = getCardStore(loc);
		if (store == null)
			throw new IllegalArgumentException("Cannot find store for " + store);
		boolean res = store.add(mcp);
		reconcile(mcp);
		return res;
	}

	public void move(MagicCardPhysical card, Location toLoc) {
		remove(card);
		card.setLocation(toLoc);
		add(card); // XXX if fails we need to undo the remove
	}

	public MagicCardPhysical split(MagicCardPhysical card, int right) {
		if (right <= 0)
			return null;
		if (right >= card.getCount())
			return null;
		int left = card.getCount() - right;
		int trade = card.getForTrade();
		int tradeLeft = 0;
		if (trade <= right) {
			tradeLeft = 0;
		} else {
			tradeLeft = trade - right;
		}
		MagicCardPhysical card2 = new MagicCardPhysical(card, card.getLocation());
		card.setCount(left);
		card.setForTrade(tradeLeft);
		card2.setCount(right);
		card2.setForTrade(trade - tradeLeft);
		Location loc = card.getLocation();
		ICardStore<IMagicCard> cardStore = getCardStore(loc);
		if (cardStore == null)
			throw new IllegalArgumentException("Cannot find store for " + cardStore);
		cardStore.update(card);
		cardStore.setMergeOnAdd(false);
		cardStore.add(card2);
		cardStore.setMergeOnAdd(true);
		updateList(cardStore.getCards(card.getCardId()));
		return card2;
	}

	public void update(MagicCardPhysical mcp) {
		Location loc = mcp.getLocation();
		ICardStore<IMagicCard> store = getCardStore(loc);
		if (store == null)
			throw new IllegalArgumentException("Cannot find store for " + store);
		store.update(mcp);
		reconcile(mcp);
	}

	public void update(MagicCard mc) {
		getMagicDBStore().update(mc);
	}

	public void update(ICardStore cardStore, MagicCardPhysical mc) {
		cardStore.update(mc);
		reconcile(mc);
	}

	public void update(IMagicCard card) {
		if (card instanceof MagicCard) {
			update((MagicCard) card);
		} else if (card instanceof MagicCardPhysical) {
			update((MagicCardPhysical) card);
		} else {
			throw new IllegalArgumentException();
		}
	}

	public void updateList(Collection<IMagicCard> list) {
		if (list == null || list.isEmpty()) {
			getMagicDBStore().updateList(null);
			reconcile();
		} else {
			IMagicCard card = list.iterator().next();
			if (card instanceof ILocatable) {
				Location loc = ((ILocatable) card).getLocation();
				ICardStore<IMagicCard> store = getCardStore(loc);
				if (store == null)
					throw new IllegalArgumentException("Cannot find store for " + loc);
				store.updateList(list);
			} else {
				getMagicDBStore().updateList(list);
			}
			reconcile(list);
		}
	}

	public Collection<MagicCardPhysical> materialize(Collection<IMagicCard> cards, ICardStore<IMagicCard> from) {
		ArrayList<MagicCardPhysical> res = new ArrayList<MagicCardPhysical>();
		ArrayList<MagicCardPhysical> in = new ArrayList<MagicCardPhysical>();
		CardGroup.expandGroups(in, cards, new Predicate<Object>() {
			@Override
			public boolean test(Object card) {
				if (card instanceof MagicCardPhysical)
					return true;
				return false;
			}
		});
		for (MagicCardPhysical card : in) {
			if (card.isOwn()) {
				res.add(new MagicCardPhysical(card, card.getLocation()));
				continue;
			}
			Collection<IMagicCard> piles = from.getCards(card.getCardId());
			MagicCardPhysical x = new MagicCardPhysical(card, null);
			if (piles == null || piles.size() == 0) {
				x.setOwn(false);
				res.add(x);
				continue;
			}
			int rem = card.getCount();
			for (IMagicCard cand : piles) {
				if (rem <= 0)
					break;
				if (cand instanceof MagicCardPhysical) {
					MagicCardPhysical mcp = (MagicCardPhysical) cand;
					int mc = mcp.getCount();
					if (mc <= 0 || mcp.isOwn() == false)
						continue;
					if (mc > rem)
						mc = rem;
					res.add(new MagicCardPhysical(mcp, mcp.getLocation()));
					rem = rem - mc;
				}
			}
			if (rem > 0) {
				x.setOwn(false);
				x.setCount(rem);
				res.add(x);
			}
		}
		return res;
	}

	/**
	 * Repairs back link between base cards and physical cards, expensive since it reads whole database
	 */
	public void reconcile() {
		links.clear();
		ICardStore lib = getLibraryCardStore();
		ICardStore db = getMagicDBStore();
		db.initialize();
		reconcile(lib);
	}

	public void reconcile(Iterable cards) {
		ICardStore db = getMagicDBStore();
		ICardStore library = DataManager.getLibraryCardStore();
		for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
			Object card = iterator.next();
			// Need to repair references to MagicCard instances
			if (card instanceof MagicCardPhysical) {
				MagicCardPhysical mcp = (MagicCardPhysical) card;
				if (mcp.getName() != null) {
					reconcile(mcp, db, library);
				}
			}
		}
	}

	private void reconcile(MagicCardPhysical mcp) {
		reconcile(mcp, getMagicDBStore(), DataManager.getLibraryCardStore());
	}

	private void reconcile(MagicCardPhysical mcp, ICardStore db, ICardStore library) {
		// System.err.println("reconcile " + mcp + " " +
		// System.identityHashCode(mcp));
		int id = mcp.getCardId();
		if (id == 0)
			return;
		MagicCard base = (MagicCard) db.getCard(id);
		if (base != null) {
			mcp.setMagicCard(base);
		} else {
			MagicLogger.log("Cannot reconsile " + mcp);
		}
		CardGroup realcards = new CardGroup(MagicCardField.ID, mcp.getName());
		realcards.addAll(library.getCards(id));
		links.put(id, realcards);
		update(mcp.getBase());
	}


	public CardGroup getRealCards(MagicCard mc) {
		int id = mc.getCardId();
		if (links.containsKey(id))
			return (CardGroup) links.get(id);
		return null;
	}

	public void setOwnCopyEnabled(boolean newValue) {
		owncopy = newValue;
	}

	public static boolean waitForInit(int sec) {
		IDbCardStore<IMagicCard> magicDBStore = getMagicDBStore();
		synchronized (magicDBStore) {
			if (!magicDBStore.isInitialized())
				asyncInitDb();
			while (!magicDBStore.isInitialized() && sec-- > 0) {
				try {
					magicDBStore.wait(1000);
				} catch (InterruptedException e) {
					break;
				}
			}
			return magicDBStore.isInitialized();
		}
	}

	public static void asyncInitDb() {
		new Thread("Init DB") {
			@Override
			public void run() {
				getMagicDBStore().initialize();
				getDBPriceStore().initialize();
				getDBPriceStore().reloadPrices(); // XXX
			}
		}.start();
	}

	public static File getPricesDir() {
		File dir = getModelRoot().getMagicDBContainer().getFile();
		File pricesDir = new File(dir, "prices");
		if (!pricesDir.exists())
			pricesDir.mkdirs();
		return pricesDir;
	}

	public static File getTablesDir() {
		File dir = getModelRoot().getMagicDBContainer().getFile();
		File pricesDir = new File(dir, "tables");
		if (!pricesDir.exists())
			pricesDir.mkdirs();
		return pricesDir;
	}
}
