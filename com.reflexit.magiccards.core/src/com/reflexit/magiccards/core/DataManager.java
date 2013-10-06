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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICardHandler;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.IMagicCardPhysical;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.nav.ModelRoot;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;
import com.reflexit.magiccards.core.model.utils.IntHashtable;

public class DataManager {
	public static final String ID = "com.reflexit.magiccards.core";
	private static ICardHandler handler;
	private static ModelRoot root;
	private static IntHashtable links = new IntHashtable();
	private static boolean owncopy;
	static {
		try {
			// String variant1 =
			// "com.reflexit.magiccards.core.sql.handlers.CardHolder";
			String variant2 = "com.reflexit.magiccards.core.xml.XmlCardHolder";
			@SuppressWarnings("rawtypes") Class c = Class.forName(variant2);
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

	public synchronized static ICardHandler getCardHandler() {
		return handler;
	}

	public static ICardStore getLibraryCardStore() {
		return handler.getLibraryCardStore();
	}

	public static IDbCardStore<IMagicCard> getMagicDBStore() {
		return handler.getMagicDBStore();
	}

	public static synchronized ModelRoot getModelRoot() {
		if (root == null) {
			root = ModelRoot.getInstance(new File(FileUtils.getWorkspaceFile(), "magiccards"));
		}
		return root;
	}

	public static ICardStore<IMagicCard> getCardStore(Location to) {
		return getCardHandler().getCardStore(to);
	}

	public static void reset(File dir) {
		// File locDir = new File(FileUtils.getWorkspaceFile(), "magiccards");
		root = ModelRoot.getInstance(dir);
	}

	public static void reset() {
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

	public static boolean copyCards(Collection cards1, Location to) {
		ArrayList<IMagicCard> cards = new ArrayList<IMagicCard>(cards1.size());
		CardGroup.expandGroups(cards, cards1);
		ICardStore<IMagicCard> store = getCardStore(to);
		if (store == null)
			return false;
		boolean virtual = store.isVirtual();
		ArrayList<IMagicCard> list = new ArrayList<IMagicCard>(cards.size());
		for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			if (owncopy == false && card instanceof MagicCardPhysical && virtual == false) {
				throw new MagicException(
						"Cannot copy real cards into non-virtual deck, use move instead - or override this protection in preferences");
			}
			// copied cards will have collection ownership for virtual
			MagicCardPhysical phi = new MagicCardPhysical(card, to, virtual);
			list.add(phi);
		}
		return add(store, list);
	}

	/**
	 * Using card representation create proper link to base or find actuall base card to replace
	 * fake one
	 * 
	 * @param input
	 * @return
	 */
	public static Collection<IMagicCard> instantiate(Collection<IMagicCard> input) {
		return instantiate(input, new ArrayList<IMagicCard>(input.size()), getMagicDBStore());
	}

	private static Collection<IMagicCard> instantiate(Collection<IMagicCard> input, Collection<IMagicCard> output, ICardStore db) {
		for (Iterator iterator = input.iterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			if (card instanceof CardGroup) {
				instantiate((Collection<IMagicCard>) ((CardGroup) card).getChildrenList(), output, db);
			} else {
				// Need to repair references to MagicCard instances
				IMagicCard cardRes = instantiate(card, db);
				if (cardRes != null)
					output.add(cardRes);
			}
		}
		return output;
	}

	public static IMagicCard instantiate(IMagicCard card, ICardStore db) {
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

	public static boolean moveCards(Collection cards1, Location to) {
		ArrayList<IMagicCard> cards = new ArrayList<IMagicCard>(cards1.size());
		CardGroup.expandGroups(cards, cards1);
		ICardStore<IMagicCard> sto = getCardStore(to);
		if (sto == null)
			return false;
		boolean virtual = sto.isVirtual();
		ArrayList<IMagicCard> list = new ArrayList<IMagicCard>(cards.size());
		for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			MagicCardPhysical phi = new MagicCardPhysical(card, to, virtual);
			if (card instanceof MagicCardPhysical) {
				if (((IMagicCardPhysical) card).isOwn()) {
					if (virtual)
						throw new MagicException("Cannot move own cards to virtual collection. Use copy instead.");
					phi.setOwn(true);
				}
			}
			list.add(phi);
		}
		boolean res = add(sto, list);
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

	public static boolean add(ICardStore store, Collection list) {
		boolean res = store.addAll(list);
		reconcile(list);
		return res;
	}

	public static void remove(ICardStore store, Collection list) {
		reconcileRemove(list);
		store.removeAll(list);
	}

	public static void remove(MagicCardPhysical mcp) {
		Location from = mcp.getLocation();
		ICardStore<IMagicCard> store = getCardStore(from);
		if (store == null)
			throw new IllegalArgumentException("Cannot find store for " + from);
		reconcileRemove(mcp);
		store.remove(mcp);
	}

	public static boolean add(MagicCardPhysical mcp) {
		Location loc = mcp.getLocation();
		ICardStore<IMagicCard> store = getCardStore(loc);
		if (store == null)
			throw new IllegalArgumentException("Cannot find store for " + store);
		boolean res = store.add(mcp);
		reconcile(mcp);
		return res;
	}

	public static void move(MagicCardPhysical card, Location toLoc) {
		DataManager.remove(card);
		card.setLocation(toLoc);
		DataManager.add(card); // XXX if fails we need to undo the remove
	}

	public static void split(MagicCardPhysical card, int left, int right) {
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
		DataManager.reconcile(cardStore.getCards(card.getCardId()));
	}

	public static void update(MagicCardPhysical mcp) {
		Location loc = mcp.getLocation();
		ICardStore<IMagicCard> store = getCardStore(loc);
		if (store == null)
			throw new IllegalArgumentException("Cannot find store for " + store);
		store.update(mcp);
		reconcile(mcp);
	}

	public static void update(MagicCard mc) {
		getMagicDBStore().update(mc);
	}

	public static void update(ICardStore cardStore, MagicCardPhysical mc) {
		cardStore.update(mc);
		reconcile(mc);
	}

	public static void update(IMagicCard card) {
		if (card instanceof MagicCard) {
			update((MagicCard) card);
		} else if (card instanceof MagicCardPhysical) {
			update((MagicCardPhysical) card);
		} else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Repairs back link between base cards and physical cards, expensive since it reads whole
	 * database
	 */
	public static void reconcile() {
		links.clear();
		ICardStore lib = handler.getLibraryCardStore();
		ICardStore db = getMagicDBStore();
		db.initialize();
		reconcile(lib);
	}

	public static void reconcile(Iterable cards) {
		ICardStore db = getMagicDBStore();
		for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
			Object card = iterator.next();
			// Need to repair references to MagicCard instances
			if (card instanceof MagicCardPhysical) {
				MagicCardPhysical mcp = (MagicCardPhysical) card;
				reconcile(mcp, db);
			}
		}
	}

	private static void reconcile(MagicCardPhysical mcp) {
		reconcile(mcp, getMagicDBStore());
	}

	private static void reconcile(MagicCardPhysical mcp, ICardStore db) {
		// System.err.println("reconcile " + mcp + " " + System.identityHashCode(mcp));
		int id = mcp.getCardId();
		MagicCard base = (MagicCard) db.getCard(id);
		if (base != null) {
			mcp.setMagicCard(base);
		} else {
			MagicLogger.log("Cannot reconsile " + mcp);
		}
		CardGroup realcards = (CardGroup) links.get(id);
		if (realcards == null) {
			realcards = new CardGroup(MagicCardField.ID, mcp.getName());
			links.put(id, realcards);
		} else {
			realcards.remove(mcp);
		}
		realcards.add(mcp);
	}

	private static void reconcileRemove(Iterable cards) {
		for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
			Object card = iterator.next();
			// Need to repair references to MagicCard instances
			if (card instanceof MagicCardPhysical)
				reconcileRemove((MagicCardPhysical) card);
		}
	}

	private static void reconcileRemove(MagicCardPhysical mcp) {
		if (mcp.getBase() == null)
			return;
		int id = mcp.getCardId();
		CardGroup realcards = (CardGroup) links.get(id);
		if (realcards != null) {
			realcards.remove(mcp);
			if (realcards.size() == 0) {
				links.remove(id);
			}
		}
	}

	public static CardGroup getRealCards(MagicCard mc) {
		return (CardGroup) links.get(mc.getCardId());
	}

	public static void setOwnCopyEnabled(boolean newValue) {
		owncopy = newValue;
	}
}
