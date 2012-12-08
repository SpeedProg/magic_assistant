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
package com.reflexit.magiccards.core.exports;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Editions.Edition;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.nav.CardCollection;
import com.reflexit.magiccards.core.model.nav.CardElement;
import com.reflexit.magiccards.core.model.nav.CollectionsContainer;
import com.reflexit.magiccards.core.model.nav.LocationPath;
import com.reflexit.magiccards.core.model.nav.ModelRoot;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.sync.TextPrinter;

/**
 * Utils to perform import
 */
public class ImportUtils {
	public static Collection<IMagicCard> performPreImport(InputStream st, IImportDelegate worker, boolean header, Location location,
			ICoreProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		if (st != null) {
			DataManager.getMagicDBStore().initialize();
			worker.init(st, false, location);
			worker.setHeader(header);
			worker.run(monitor);
			Collection<IMagicCard> importedCards = worker.getImportedCards();
			return importedCards;
		} else
			return null;
	}

	public static void performImport(InputStream st, IImportDelegate worker, boolean header, Location location, ICardStore cardStore,
			ICoreProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		Collection importedCards = performPreImport(st, worker, header, location, monitor);
		performImport(importedCards, cardStore);
	}

	public static void performImport(Collection importedCards, ICardStore cardStore) {
		if (importedCards != null) {
			Collection<Location> importedLocations = getLocations(importedCards);
			createDecks(importedLocations);
			// set the hard reference from database
			for (Iterator iterator = importedCards.iterator(); iterator.hasNext();) {
				IMagicCard card = (IMagicCard) iterator.next();
				if (card instanceof MagicCardPhysical)
					((MagicCardPhysical) card).setMagicCard((MagicCard) card.getBase());
			}
			// import into card store
			cardStore.addAll(importedCards);
			DataManager.reconcileAdd(importedCards);
		}
	}

	private static void createDecks(Collection<Location> importedLocations) {
		for (Iterator iterator = importedLocations.iterator(); iterator.hasNext();) {
			Location location = (Location) iterator.next();
			if (location.isSideboard()) {
				ModelRoot root = DataManager.getModelRoot();
				String containerName = location.getParent().getPath();
				if (containerName.startsWith(File.separator))
					containerName = containerName.substring(File.separator.length());
				final CardElement resource = root.findElement(new LocationPath(containerName));
				if (!(resource instanceof CollectionsContainer)) {
					continue; // ???
				}
				CollectionsContainer parent = (CollectionsContainer) resource;
				if (parent.contains(location)) {
					continue;
				}
				CardCollection sideboard = parent.addDeck(location.getBaseFileName());
				CardCollection maindeck = (CardCollection) parent.findChield(location.toMainDeck());
				if (maindeck != null)
					sideboard.setVirtual(maindeck.isVirtual());
				sideboard.close();
			}
		}
	}

	private static Collection<Location> getLocations(Collection importedCards) {
		HashSet<Location> res = new HashSet<Location>();
		for (Iterator iterator = importedCards.iterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			if (card instanceof MagicCardPhysical)
				res.add(((MagicCardPhysical) card).getLocation());
		}
		return res;
	}

	public static MagicCard findRef(MagicCard card) {
		ICardStore lookupStore = DataManager.getCardHandler().getMagicDBStore();
		if (lookupStore == null)
			return card;
		MagicCard cand = (MagicCard) lookupStore.getCard(card.getCardId());
		if (cand != null)
			return cand;
		String name = getFixedName(card);
		String set = getFixedSet(card);
		for (Iterator iterator = lookupStore.iterator(); iterator.hasNext();) {
			MagicCard a = (MagicCard) iterator.next();
			String lname = a.getName();
			if (name != null && name.equalsIgnoreCase(lname)) {
				if (set == null)
					return a;
				if (set.equalsIgnoreCase(a.getSet()))
					return a;
				if (cand == null || cand.getCardId() < a.getCardId())
					cand = a;
			}
		}
		if (cand != null) {
			System.err.println("Looking for " + card.getName() + " " + set + " but found " + cand.getSet());
		}
		return cand;
	}

	public static String getFixedSet(MagicCard card) {
		String set = card.getSet();
		set = resolveSet(set);
		if (set != null)
			card.setSet(set);
		return set;
	}

	public static String resolveSet(String orig1) {
		if (orig1 == null)
			return null;
		String set = orig1;
		Editions eds = Editions.getInstance();
		if (eds.getEditionByName(set) != null)
			return set;
		if (set.toLowerCase(Locale.ENGLISH).startsWith("token ")) {
			set = set.substring(6);
		} else if (set.contains("''")) {
			set = set.replaceAll("''", "\"");
		} else if (set.contains(" : ")) {
			set = set.replaceAll(" : ", ": ");
		}
		set = set.trim();
		for (Iterator<Edition> iterator = eds.getEditions().iterator(); iterator.hasNext();) {
			Edition ed = iterator.next();
			if (set.equalsIgnoreCase(ed.getName())) {
				set = ed.getName();
				break;
			}
		}
		return set;
	}

	public static String getFixedName(MagicCard card) {
		String name = card.getName();
		if (name == null)
			return null;
		if (name.contains("Aet")) // Æther
			name = name.replaceAll("Aet", "Æt");
		else
			return name;
		card.setName(name);
		return name;
	}

	public static Map<String, String> getSetCandidates(Iterable<IMagicCard> cards) {
		Editions editions = Editions.getInstance();
		HashMap<String, String> badSets = new HashMap<String, String>();
		for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			if (card.getCardId() == 0 && card.getSet() != null) {
				String set = card.getSet();
				String rset = resolveSet(set);
				Edition eset = editions.getEditionByName(rset);
				if (eset == null) {
					if (rset == null || rset.equals(set))
						rset = null;
					badSets.put(set, rset);
				}
			}
		}
		return badSets;
	}

	public static MagicCard updateCardReference(MagicCardPhysical card) {
		if (card == null)
			return null;
		MagicCard ref = findRef(card.getCard());
		if (ref != null) {
			if (card.getSet() == null || ref.getSet().equalsIgnoreCase(card.getSet())) {
				card.setMagicCard(ref);
				return null;
			} else if (card.getSet() != null) {
				MagicCard newCard = (MagicCard) ref.clone();
				newCard.setSet(card.getSet());
				newCard.setCardId(0);
				card.setMagicCard(newCard);
				card.setError("Set not found");
				return newCard;
			}
		} else {
			card.setError("Card not found in DB");
		}
		return card.getBase();
	}

	public static ImportResult performPreview(InputStream st, IImportDelegate<IMagicCard> worker, boolean header, Location loc,
			ICoreProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		worker.init(st, true, loc);
		worker.setHeader(header);
		// init preview
		ImportResult previewResult = worker.getPreview();
		worker.run(monitor);
		return previewResult;
	}

	/**
	 * Finds and associates imported cards with magic db cards. If card not found in db creates new
	 * db cards and adds to newdbrecords
	 */
	public static void performPreImportWithDb(Collection<IMagicCard> result, Collection<IMagicCard> newdbrecords) {
		for (Iterator iterator = result.iterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			if (card instanceof MagicCardPhysical) {
				MagicCard newCard = (MagicCard) card.getBase();
				newCard = ImportUtils.updateCardReference((MagicCardPhysical) card);
				if (newCard != null) {
					// import int DB
					newdbrecords.add(newCard);
				}
			}
		}
	}

	public static void validateDbRecords(ArrayList<IMagicCard> newdbrecords, ArrayList<String> lerrors) {
		int row = 0;
		for (Iterator iterator = newdbrecords.iterator(); iterator.hasNext(); row++) {
			IMagicCard card = (IMagicCard) iterator.next();
			MagicCard newCard = (MagicCard) card.getBase();
			String prefix = TextPrinter.toString(newCard) + ": Cannot import new card into db: ";
			if (newCard.getName() == null) {
				lerrors.add(prefix + " name is missing");
				iterator.remove();
			} else if (newCard.getSet() == null) {
				lerrors.add(prefix + " set is missing");
				iterator.remove();
			} else if (newCard.getType() == null) {
				lerrors.add(prefix + " type is missing");
				iterator.remove();
			} else if (newCard.getCollectorNumberId() == 0) {
				lerrors.add(prefix + " collector number is missing (required to import new card into db)");
				iterator.remove();
			}
		}
	}

	public static void importIntoDb(Collection<IMagicCard> newdbrecords) {
		ICardStore magicDbHandler = DataManager.getMagicDBStore();
		int row = 0;
		for (Iterator iterator = newdbrecords.iterator(); iterator.hasNext(); row++) {
			IMagicCard card = (IMagicCard) iterator.next();
			MagicCard newCard = (MagicCard) card.getBase();
			magicDbHandler.add(newCard);
		}
	}

	public static void fixSets(Collection<IMagicCard> result, Map<String, String> badSets) {
		for (Iterator iterator = result.iterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			String set = card.getSet();
			String corr = badSets.get(set);
			if (set != null && corr != null) {
				if (corr.equals("Skip Import")) { // XXX!
					iterator.remove();
				}
				MagicCard newCard = (MagicCard) card.getBase();
				newCard.setSet(corr);
			}
		}
	}
}
