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

	public static String getFixedSet(MagicCard card) {
		String set = card.getSet();
		Edition eset = resolveSet(set);
		if (eset != null) {
			set = eset.getName();
			card.setSet(set);
		}
		return set;
	}

	static Map<String, String> setAlias = new HashMap<String, String>();
	static {
		setAlias.put("alpha", "Limited Edition Alpha");
		setAlias.put("beta", "Limited Edition Beta");
		setAlias.put("alpha edition", "Limited Edition Alpha");
		setAlias.put("beta edition", "Limited Edition Beta");
		setAlias.put("revised", "Revised Edition");
		setAlias.put("sixth edition", "Classic Sixth Edition");
		setAlias.put("timeshifted", "Time Spiral \"Timeshifted\"");
		setAlias.put("time spiral ''timeshifted''", "Time Spiral \"Timeshifted\"");
		setAlias.put("''timeshifted''", "Time Spiral \"Timeshifted\"");
		setAlias.put("\"timeshifted\"", "Time Spiral \"Timeshifted\"");
		setAlias.put("commander", "Magic: The Gathering-Commander");
		setAlias.put("4th edition", "Fourth Edition");
		setAlias.put("5th edition", "Fifth Edition");
		setAlias.put("6th edition", "Classic Sixth Edition");
		setAlias.put("7th edition", "Seventh Edition");
		setAlias.put("8th edition", "Eighth Edition");
		setAlias.put("9th edition", "Nineth Edition");
		setAlias.put("10th edition", "Tenth Edition");
		Collection<Edition> editions = Editions.getInstance().getEditions();
		for (Iterator iterator = editions.iterator(); iterator.hasNext();) {
			Edition edition = (Edition) iterator.next();
			String lset = edition.getName().toLowerCase(Locale.ENGLISH);
			setAlias.put(lset, edition.getName());
			String nset = lset.replaceAll(" edition", "");
			setAlias.put(nset, edition.getName());
		}
	}

	public static Edition resolveSet(String origSet) {
		if (origSet == null)
			return null;
		String set = origSet.trim();
		if (set.contains(" : ")) {
			set = set.replaceAll(" : ", ": ");
		}
		String lset = set.toLowerCase(Locale.ENGLISH);
		if (lset.startsWith("token ")) {
			lset = lset.substring(6);
		}
		if (setAlias.containsKey(lset)) {
			set = setAlias.get(lset);
			return Editions.getInstance().getEditionByName(set);
		}
		return null;
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
				Edition eset = resolveSet(set);
				if (eset == null) {
					badSets.put(set, null);
				} else if (!eset.getName().equals(set)) {
					badSets.put(set, eset.getName());
				}
			}
		}
		return badSets;
	}

	public static MagicCard updateCardReference(MagicCardPhysical card) {
		if (card == null)
			return null;
		Edition ed = resolveSet(card.getSet());
		if (ed != null)
			card.getBase().setSet(ed.getName());
		MagicCard base = card.getCard();
		ICardStore lookupStore = DataManager.getCardHandler().getMagicDBStore();
		if (lookupStore == null) {
			card.setError("DB not found");
			return base;
		}
		MagicCard ref = findRef(base, lookupStore);
		if (ref != null) {
			if (card.getSet() == null || ref.getSet().equals(card.getSet())) {
				card.setMagicCard(ref);
				return null;
			} else if (card.getSet() != null) {
				if (ed == null) {
					MagicCard newCard = (MagicCard) ref.clone();
					newCard.setSet(card.getSet());
					newCard.setCardId(0);
					card.setMagicCard(newCard);
					card.setError("Set not found");
					return newCard;
				} else {
					card.setMagicCard(ref);
					card.setError("Card not found in the set");
				}
			}
		} else {
			card.setError("Card not found in DB");
		}
		return card.getBase();
	}

	public static MagicCard findRef(MagicCard base, ICardStore lookupStore) {
		MagicCard ref = null;
		// by id
		if (base.getCardId() != 0) {
			MagicCard cand = (MagicCard) lookupStore.getCard(base.getCardId());
			if (cand != null) {
				ref = cand;
			}
		}
		// by name
		if (ref == null) {
			String name = base.getName();
			String set = base.getSet();
			for (Iterator iterator = lookupStore.iterator(); iterator.hasNext();) {
				MagicCard a = (MagicCard) iterator.next();
				String lname = a.getName();
				if (name != null && name.equalsIgnoreCase(lname)) {
					if (set != null && set.equals(a.getSet())) {
						ref = a;
						break;
					}
					if (ref == null || ref.getCardId() <= a.getCardId())
						ref = a;
				}
			}
		}
		return ref;
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
