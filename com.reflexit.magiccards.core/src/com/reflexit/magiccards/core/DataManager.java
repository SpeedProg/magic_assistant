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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICardHandler;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.IMagicCardPhysical;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardList;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.Predicate;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.nav.ModelRoot;
import com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;
import com.reflexit.magiccards.core.model.storage.IDbPriceStore;
import com.reflexit.magiccards.core.model.storage.ILocatable;
import com.reflexit.magiccards.core.model.xml.DbMultiFileCardStore;
import com.reflexit.magiccards.core.model.xml.XmlCardHolder;

public class DataManager {
	public static final String ID = "com.reflexit.magiccards.core";
	public static final Set<? extends ICardField> COUNT_FIELDSET = Collections.singleton(MagicCardField.COUNT);
	private static DataManager instance = new DataManager();
	private ICardHandler handler;
	private ModelRoot root;
	private TIntObjectHashMap<IMagicCard> links = new TIntObjectHashMap<IMagicCard>();
	private boolean owncopy;
	private Thread initThread;
	private Object initThreadLock = new Object();

	private DataManager() {
		MagicLogger.debug("Data Manager instance " + this.hashCode());
		handler = new XmlCardHolder();
	}

	public static final DataManager getInstance() {
		return instance;
	}

	public static final ICardHandler getCardHandler() {
		return instance.handler;
	}

	public ICardStore getLibraryCardStore() {
		return handler.getLibraryCardStore();
	}

	public IDbCardStore<IMagicCard> getMagicDBStore() {
		return handler.getMagicDBStore();
	}

	public static IDbPriceStore getDBPriceStore() {
		return instance.handler.getDBPriceStore();
	}

	public boolean isInitialized() {
		return root != null;
	}

	public ModelRoot getModelRoot() {
		if (root != null)
			return root;
		synchronized (this) {
			if (root == null)
				root = ModelRoot.getInstance(FileUtils.getMagicCardsDir());
			return root;
		}
	}

	public ICardStore<IMagicCard> getCardStore(Location to) {
		return getCardHandler().getCardStore(to);
	}

	public void reset(File dir) {
		// File locDir = new File(FileUtils.getWorkspaceFile(), "magiccards");
		synchronized (this) {
			System.setProperty("ma.magiccards.area", dir.getAbsolutePath());
			FileUtils.deleteTree(dir);
			if (root == null) {
				root = ModelRoot.getInstance(dir);
				return;
			}
			root.resetRoot(dir);
		}
		((DbMultiFileCardStore) (getCardHandler().getMagicDBStore())).reload();
		((AbstractFilteredCardStore) (getCardHandler().getLibraryFilteredStore())).reload();
		reconcile();
	}

	public void reset() {
		reset(getRootDir());
	}

	public File getRootDir() {
		File rootDir = getModelRoot().getRootDir();
		if (rootDir == null)
			throw new NullPointerException();
		return rootDir;
	}

	boolean copyCards(Collection<IMagicCard> cards, ICardStore<IMagicCard> store, Location to) {
		if (store == null)
			throw new NullPointerException();
		boolean virtual = store.isVirtual();
		ArrayList<IMagicCard> list = new ArrayList<IMagicCard>(cards.size());
		boolean ownCopyAllowed = owncopy;
		for (Iterator<IMagicCard> iterator = cards.iterator(); iterator.hasNext();) {
			IMagicCard card = iterator.next();
			if (ownCopyAllowed == false && card instanceof MagicCardPhysical && virtual == false
					&& ((MagicCardPhysical) card).isOwn()) {
				throw new MagicException(
						"Cannot copy own cards into non-virtual deck, use move instead - or override this protection in preferences");
			}
			// copied cards will have target collection ownership
			MagicCardPhysical phi = new MagicCardPhysical(card, to, virtual);
			list.add(phi);
		}
		return add(list, store);
	}

	public boolean copyCards(Collection cards1, ICardStore<IMagicCard> sto) {
		return copyCards(cards1, sto, sto.getLocation());
	}

	/**
	 * Using card representation create proper link to base or find actuall base card to replace fake one
	 *
	 * @param input
	 * @return
	 */
	public Collection<IMagicCard> resolve(Collection<IMagicCard> input) {
		return resolve(input, new ArrayList<IMagicCard>(), getMagicDBStore());
	}

	private Collection<IMagicCard> resolve(Collection<IMagicCard> input, Collection<IMagicCard> output, ICardStore db) {
		for (Iterator iterator = input.iterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			if (card instanceof CardGroup) {
				resolve(((CardGroup) card).getChildren(), output, db);
			} else {
				// Need to repair references to MagicCard instances
				IMagicCard cardRes = resolve(card, db);
				if (cardRes != null)
					output.add(cardRes);
			}
		}
		return output;
	}

	private Collection<IMagicCard> resolve(ICard[] input, Collection<IMagicCard> output, ICardStore db) {
		for (ICard card : input) {
			if (card instanceof CardGroup) {
				resolve(((CardGroup) card).getChildren(), output, db);
			} else {
				// Need to repair references to MagicCard instances
				IMagicCard cardRes = resolve((IMagicCard) card, db);
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

	public boolean moveCards(Collection<IMagicCard> cards1, ICardStore<IMagicCard> sto) {
		return moveCards(cards1, sto, sto.getLocation());
	}

	boolean moveCards(Collection cards, ICardStore<IMagicCard> store, Location to) {
		if (store == null)
			throw new NullPointerException();
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
		boolean res = add(list, store);
		if (res) {
			Location from = getLocation(cards);
			if (from != null) { // optimization
				ICardStore<IMagicCard> sfrom2 = getCardStore(from);
				if (sfrom2 != null) {
					remove(cards, sfrom2);
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

	public Location getLocation(Collection<IMagicCard> cards) {
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
					from = null;
					break;
				}
			}
		}
		return from;
	}

	public List<IMagicCard> splitCards(Collection<IMagicCard> cards1, int count) {
		Collection<IMagicCard> cards = expandGroups(cards1);
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

	public boolean add(Collection list, ICardStore store) {
		boolean res = store.addAll(list);
		reconcile(list);
		return res;
	}

	public void remove(Collection list, ICardStore store) {
		if (list == null) {
			store.removeAll();
			reconcile();/// XXX
		} else {
			store.removeAll(list);
			reconcile(list);
		}
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
		if (!card.isMigrated())
			throw new IllegalArgumentException("Has to be migrated first");
		if (right <= 0)
			return null;
		if (right >= card.getCount())
			return null;
		Location loc = card.getLocation();
		ICardStore<IMagicCard> cardStore = getCardStore(loc);
		if (cardStore == null)
			throw new IllegalArgumentException("Cannot find store for " + cardStore);
		int left = card.getCount() - right;
		MagicCardPhysical card2 = new MagicCardPhysical(card, card.getLocation());
		card.setCount(left);
		card2.setCount(right);
		Set<MagicCardField> fieldSet = Collections.singleton(MagicCardField.COUNT);
		cardStore.update(card, fieldSet);
		cardStore.setMergeOnAdd(false);
		cardStore.add(card2);
		cardStore.setMergeOnAdd(true);
		updateList(cardStore.getCards(card.getCardId()), fieldSet);
		return card2;
	}

	public void updateMCP(MagicCardPhysical mcp, Set<? extends ICardField> fieldSet) {
		Location loc = mcp.getLocation();
		ICardStore<IMagicCard> store = getCardStore(loc);
		if (store == null)
			throw new IllegalArgumentException("Cannot find store for " + store);
		store.update(mcp, fieldSet);
		reconcile(mcp);
	}

	public void updateMC(MagicCard mc, Set<? extends ICardField> fieldSet) {
		getMagicDBStore().update(mc, fieldSet);
	}

	public void setField(MagicCardPhysical mc, ICardField field, Object newValue) {
		setField(null, mc, field, newValue);
	}

	public void setField(ICardStore cardStore, MagicCardPhysical mc, ICardField field, Object newValue) {
		Object oldValue = mc.get(field);
		if ((newValue != null && newValue.equals(oldValue)) || (newValue == null && oldValue == null))
			return;
		mc.set(field, newValue);
		if (cardStore == null) {
			Location loc = mc.getLocation();
			cardStore = getCardStore(loc);
			if (cardStore == null)
				throw new IllegalArgumentException("Cannot find store for " + cardStore);
		}
		cardStore.update(mc, Collections.singleton(field));
		reconcile(mc);
	}

	public void update(ICardStore<IMagicCard> cardStore, Set<? extends ICardField> fieldSet) {
		cardStore.updateList(null, fieldSet);
		reconcile(cardStore);
	}
	public void update(IMagicCard card, Set<? extends ICardField> fieldSet) {
		if (card instanceof MagicCard) {
			updateMC((MagicCard) card, fieldSet);
		} else if (card instanceof MagicCardPhysical) {
			updateMCP((MagicCardPhysical) card, fieldSet);
		} else {
			// ignore
		}
	}

	public void updateList(Collection<IMagicCard> list, Set<? extends ICardField> fieldSet) {
		if (list == null || list.isEmpty()) {
			getMagicDBStore().updateList(null, fieldSet);
			reconcile();
		} else {
			IMagicCard card = list.iterator().next();
			if (card instanceof ILocatable) {
				Location loc = ((ILocatable) card).getLocation();
				ICardStore<IMagicCard> store = getCardStore(loc);
				if (store == null)
					throw new IllegalArgumentException("Cannot find store for " + loc);
				store.updateList(list, fieldSet);
			} else {
				getMagicDBStore().updateList(list, fieldSet);
			}
			reconcile(list);
		}
	}

	public Collection<MagicCardPhysical> materialize(Collection<? extends IMagicCard> cards,
			ICardStore<IMagicCard> from) {
		return materialize(cards, Collections.singleton(from));
	}

	public Collection<MagicCardPhysical> materialize(Collection<? extends IMagicCard> cards,
			Collection<ICardStore<IMagicCard>> stores) {
		ArrayList<MagicCardPhysical> in = new ArrayList<MagicCardPhysical>();
		DataManager.expandGroups(in, cards, new Predicate<Object>() {
			@Override
			public boolean test(Object card) {
				if (card instanceof MagicCardPhysical)
					return true;
				return false;
			}
		});
		ArrayList<MagicCardPhysical> res = new ArrayList<MagicCardPhysical>();
		for (MagicCardPhysical card : in) {
			materialize(card, stores, res);
		}
		return res;
	}

	public ArrayList<MagicCardPhysical> materialize(MagicCardPhysical card, Collection<ICardStore<IMagicCard>> stores,
			ArrayList<MagicCardPhysical> res) {
		if (card.isOwn()) {
			res.add(new MagicCardPhysical(card, null));
			return res;
		}
		int rem = card.getCount();
		for (ICardStore<IMagicCard> from : stores) {
			Collection<IMagicCard> piles = from.getCards(card.getCardId());
			if (piles == null || piles.size() == 0) {
				continue;
			}
			for (IMagicCard cand : piles) {
				if (rem <= 0)
					break;
				if (cand instanceof MagicCardPhysical) {
					MagicCardPhysical mcp = (MagicCardPhysical) cand;
					int mc = mcp.getCount();
					if (mc == 0 || mcp.isOwn() == false)
						continue;
					if (mc > rem)
						mc = rem;
					MagicCardPhysical grab = new MagicCardPhysical(mcp, mcp.getLocation());
					grab.setCount(mc);
					grab.setOwn(true);
					res.add(grab);
					rem = rem - mc;
				}
			}
		}
		if (rem > 0) {
			MagicCardPhysical x = new MagicCardPhysical(card, null);
			x.setOwn(false);
			x.setCount(rem);
			res.add(x);
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
		if (cards == null)
			return;
		ICardStore db = getMagicDBStore();
		ICardStore library = getLibraryCardStore();
		List<IMagicCard> list = new MagicCardList(cards).getList();
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Object card = iterator.next();
			// Need to repair references to MagicCard instances
			if (card instanceof MagicCardPhysical) {
				MagicCardPhysical mcp = (MagicCardPhysical) card;
				if (mcp.getName() != null) {
					reconcile(mcp, db, library, false);
				}
			}
		}
		Collection<IMagicCard> list2 = new MagicCardList(list).getMagicBaseList();
		getMagicDBStore().updateList(list2, Collections.singleton(MagicCardField.OWN_COUNT));
	}

	private void reconcile(MagicCardPhysical mcp) {
		reconcile(mcp, getMagicDBStore(), getLibraryCardStore(), true);
	}

	private void reconcile(MagicCardPhysical mcp, ICardStore db, ICardStore library, boolean update) {
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
		if (update)
			update(mcp.getBase(), Collections.singleton(MagicCardField.OWN_COUNT));
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

	public boolean waitForInit(int sec) {
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

	public void syncInitDb() {
		MagicLogger.traceStart("syncDb");
		getModelRoot();
		getMagicDBStore().initialize();
		getDBPriceStore().initialize();
		getDBPriceStore().reloadPrices(); // XXX
		getCardHandler().getLibraryCardStore().initialize();
		reconcile();
		MagicLogger.traceEnd("syncDb");
	}

	public void asyncInitDb() {
		synchronized (initThreadLock) {
			if (initThread == null) {
				MagicLogger.trace("sync db thread");
				initThread = new Thread("Init DB") {
					@Override
					public void run() {
						syncInitDb();
						initThread = null;
					}
				};
				initThread.start();
			} else {
				MagicLogger.trace("sync db bailed");
				return;
			}
		}
	}

	public File getPricesDir() {
		File dir = getModelRoot().getMagicDBContainer().getFile();
		File pricesDir = new File(dir, "prices");
		if (!pricesDir.exists())
			pricesDir.mkdirs();
		return pricesDir;
	}

	public File getTablesDir() {
		File dir = getModelRoot().getMagicDBContainer().getFile();
		File pricesDir = new File(dir, "tables");
		if (!pricesDir.exists())
			pricesDir.mkdirs();
		return pricesDir;
	}

	public static Collection expandGroups(Collection cards) {
		return expandGroups(new ArrayList(cards.size()), cards, new CardGroup.NonGroupPredicate());
	}

	public static Collection expandGroups(Collection result, Collection cards) {
		return expandGroups(result, cards, new CardGroup.NonGroupPredicate());
	}

	public static Collection expandGroups(Collection result, Collection cards, Predicate<Object> filter) {
		for (Object o : cards) {
			if (filter.test(o))
				result.add(o);
			if (o instanceof CardGroup)
				expandGroups(result, ((CardGroup) o).getChildren(), filter);
		}
		return result;
	}

	public static Collection expandGroups(Collection result, ICard[] cards, Predicate<Object> filter) {
		for (ICard o : cards) {
			if (filter.test(o))
				result.add(o);
			if (o instanceof CardGroup)
				expandGroups(result, ((CardGroup) o).getChildren(), filter);
		}
		return result;
	}
}
