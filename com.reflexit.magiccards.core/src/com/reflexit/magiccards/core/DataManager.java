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
	static {
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

	public synchronized static ICardHandler getCardHandler() {
		return handler;
	}

	public static ICardStore getLibraryCardStore() {
		return handler.getLibraryCardStore();
	}

	public static IDbCardStore getMagicDBStore() {
		return handler.getMagicDBStore();
	}

	public static synchronized ModelRoot getModelRoot() {
		if (root == null) {
			root = ModelRoot.getInstance(new File(FileUtils.getWorkspaceFile(), "magiccards"));
		}
		return root;
	}

	public static ICardStore getCardStore(Location to) {
		return getCardHandler().getCardStore(to);
	}

	public static void reset(File dir) {
		root = ModelRoot.getInstance(new File(FileUtils.getWorkspaceFile(), "magiccards"));
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
			MagicCardPhysical phi = new MagicCardPhysical(card, to);
			if (card instanceof MagicCard) // moving from db
				phi.setOwn(!virtual);
			else if (virtual) {
				phi.setOwn(false); // copied cards will have collection
									// ownership for virtual
			}
			list.add(phi);
		}
		return add(store, list);
	}

	public static boolean moveCards(Collection cards1, Location from, Location to) {
		ArrayList<IMagicCard> cards = new ArrayList<IMagicCard>(cards1.size());
		CardGroup.expandGroups(cards, cards1);
		ICardStore<IMagicCard> sto = getCardStore(to);
		if (sto == null)
			return false;
		boolean virtual = sto.isVirtual();
		ArrayList<IMagicCard> list = new ArrayList<IMagicCard>(cards.size());
		for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			MagicCardPhysical phi = new MagicCardPhysical(card, to);
			if (card instanceof MagicCard) {
				phi.setOwn(!virtual);
			} else if (card instanceof MagicCardPhysical) {
				if (((IMagicCardPhysical) card).isOwn() && virtual)
					throw new MagicException("Cannot move own cards to virtual collection. Use copy instead.");
			}
			list.add(phi);
		}
		boolean res = add(sto, list);
		if (res) {
			boolean allthesame = true;
			if (from == null) {
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
		reconcileRemove(mcp);
		Location from = mcp.getLocation();
		ICardStore<IMagicCard> sfrom = getCardStore(from);
		sfrom.remove(mcp);
	}

	public static void update(MagicCardPhysical mc) {
		Location loc = mc.getLocation();
		ICardStore<IMagicCard> store = getCardStore(loc);
		store.update(mc);
		reconcile(mc);
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

	public static void reconcile(MagicCardPhysical mcp) {
		reconcile(mcp, getMagicDBStore());
	}

	public static void reconcile(MagicCardPhysical mcp, ICardStore db) {
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

	public static void reconcileRemove(Iterable cards) {
		for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
			Object card = iterator.next();
			// Need to repair references to MagicCard instances
			if (card instanceof MagicCardPhysical)
				reconcileRemove((MagicCardPhysical) card);
		}
	}

	public static void reconcileRemove(MagicCardPhysical mcp) {
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
}
