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
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.nav.ModelRoot;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;
import com.reflexit.magiccards.core.model.utils.IntHashtable;
import com.thoughtworks.xstream.XStream;

public class DataManager {
	public static final String ID = "com.reflexit.magiccards.core";
	private static ICardHandler handler;
	private static ModelRoot root;
	private static File rootDir;
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

	public static void reconcile() {
		links.clear();
		ICardStore lib = handler.getLibraryCardStore();
		ICardStore db = getMagicDBStore();
		db.initialize();
		reconcileAdd(lib);
	}

	public static void reconcileAdd(Iterable cards) {
		ICardStore db = getMagicDBStore();
		for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
			Object card = iterator.next();
			// Need to repair references to MagicCard instances
			if (card instanceof MagicCardPhysical) {
				MagicCardPhysical mcp = (MagicCardPhysical) card;
				int id = mcp.getCardId();
				IMagicCard base = (IMagicCard) db.getCard(id);
				if (base != null && base != mcp.getCard()) {
					mcp.setMagicCard((MagicCard) base);
				}
				if (base == null) {
					System.err.println("Cannot reconsile " + mcp);
					base = mcp.getBase();
				}
				CardGroup realcards = (CardGroup) links.get(id);
				if (realcards == null) {
					realcards = new CardGroup(MagicCardField.ID, base.getName());
					links.put(id, realcards);
				}
				realcards.add(mcp);
			}
		}
	}

	public static void reconcileRemove(Iterable cards) {
		ICardStore db = getMagicDBStore();
		for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
			Object card = iterator.next();
			// Need to repair references to MagicCard instances
			if (card instanceof MagicCardPhysical) {
				MagicCardPhysical mcp = (MagicCardPhysical) card;
				int id = mcp.getCardId();
				CardGroup realcards = (CardGroup) links.get(id);
				if (realcards != null) {
					realcards.remove(mcp);
					if (realcards.size() <= 0) {
						links.remove(id);
					}
				}
			}
		}
	}

	public static CardGroup getRealCards(MagicCard mc) {
		return (CardGroup) links.get(mc.getCardId());
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
			if (rootDir == null)
				DataManager.setRootDir(new File(FileUtils.getWorkspaceFile(), "magiccards"));
			root = ModelRoot.getInstance();
		}
		return root;
	}

	public static void setRootDir(File dir) {
		rootDir = dir;
	}

	public static File getRootDir() {
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
		boolean res = store.addAll(list);
		DataManager.reconcileAdd(list);
		return res;
	}

	public static ICardStore getCardStore(Location to) {
		return getCardHandler().getCardStore(to);
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
		boolean res = sto.addAll(list);
		reconcileAdd(list);
		ArrayList<IMagicCard> remove = new ArrayList<IMagicCard>();
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
					sfrom2.removeAll(cards);
					remove.addAll(cards);
				}
			} else {
				for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
					IMagicCard card = (IMagicCard) iterator.next();
					if (!(card instanceof MagicCardPhysical))
						continue;
					Location from2 = ((MagicCardPhysical) card).getLocation();
					ICardStore<IMagicCard> sfrom2 = getCardStore(from2);
					sfrom2.remove(card);
					remove.add(card);
				}
			}
			reconcileRemove(remove);
		}
		return res;
	}

	public static XStream getXStream() {
		XStream xstream = new XStream();
		xstream.alias("mc", MagicCard.class);
		xstream.alias("mcp", MagicCardPhysical.class);
		xstream.alias("pfield", MagicCardFieldPhysical.class);
		return xstream;
	}

	public static void remove(ICardStore store, Collection list) {
		DataManager.reconcileRemove(list);
		store.removeAll(list);
	}
}
