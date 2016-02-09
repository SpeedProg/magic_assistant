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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.Edition;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardList;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.ICard;
import com.reflexit.magiccards.core.model.abs.ICardField;
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
	public static ImportData performPreImport(
			IImportDelegate worker,
			ImportData result,
			ICoreProgressMonitor monitor)
			throws InvocationTargetException,
			InterruptedException {
		worker.init(result);
		worker.run(monitor);
		return result;
	}

	public static void resolve(List<? extends ICard> list) {
		DataManager.getInstance().getMagicDBStore().initialize();
		for (ICard card : list) {
			if (card instanceof MagicCardPhysical)
				ImportUtils.updateCardReference((MagicCardPhysical) card);
		}
	}

	public static void performImport(Collection importedCards, ICardStore cardStore) {
		if (importedCards != null) {
			Collection<Location> importedLocations = getLocations(importedCards);
			createDecks(importedLocations);
			// import into card store
			DataManager.getInstance().add(importedCards, cardStore);
		}
	}

	private static void createDecks(Collection<Location> importedLocations) {
		for (Iterator iterator = importedLocations.iterator(); iterator.hasNext();) {
			Location location = (Location) iterator.next();
			if (location.isSideboard()) {
				ModelRoot root = DataManager.getInstance().getModelRoot();
				String containerName = location.getParent().getPath();
				final CardElement resource = root.findElement(new LocationPath(containerName));
				if (!(resource instanceof CollectionsContainer)) {
					continue; // ???
				}
				CollectionsContainer parent = (CollectionsContainer) resource;
				if (parent.contains(location)) {
					continue;
				}
				boolean virtual = true;
				CardCollection maindeck = (CardCollection) parent.findChield(location.toMainDeck());
				if (maindeck != null) {
					virtual = maindeck.isVirtual();
				}
				parent.addDeck(location.getBaseFileName(), virtual);
			}
		}
	}

	private static Collection<Location> getLocations(Collection importedCards) {
		return new MagicCardList(importedCards).<Location> getUnique(MagicCardField.LOCATION);
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

	static Map<String, Edition> setAlias = new HashMap<>();

	public static Edition resolveSet(String origSet) {
		if (origSet == null)
			return null;
		if (setAlias.containsKey(origSet))
			return setAlias.get(origSet);
		String set = origSet.trim();
		if (set.contains(" : ")) {
			set = set.replaceAll(" : ", ": ");
		}
		if (set.contains("''")) {
			set = set.replaceAll("''", "\"");
		}
		if (set.startsWith("token ") || set.startsWith("Token ")) {
			set = set.substring(6);
		}
		Edition eset = Editions.getInstance().getEditionByName(set);
		if (eset != null) {
			setAlias.put(origSet, eset);
			return eset;
		}
		eset = Editions.getInstance().getEditionByNameIgnoreCase(set);
		if (eset != null) {
			setAlias.put(origSet, eset);
			return eset;
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
		HashMap<String, String> badSets = new HashMap<String, String>();
		cards: for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			String set = card.getSet();
			if (set == null) continue;
			Edition eset = resolveSet(set);
			if (eset == null) {
				badSets.put(set, null);
			} else if (!eset.getName().equals(set)) {
				for (String alias : eset.getAliases()) {
					if (alias.equals(set)) {
						continue cards;
					}
				}
				badSets.put(set, eset.getName());
			}
		}
		return badSets;
	}

	public static void updateCardReference(Collection<ICard> result) {
		for (Iterator iterator = result.iterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			if (card instanceof MagicCardPhysical) {
				ImportUtils.updateCardReference((MagicCardPhysical) card);
			}
		}
	}

	public static MagicCard updateCardReference(MagicCardPhysical card) {
		if (card == null)
			return null;
		String originalSet = card.getSet();
		Edition ed = resolveSet(originalSet);
		if (ed != null) {
			card.getBase().setSet(ed.getName());
			originalSet = ed.getName();
		}
		MagicCard base = card.getCard();
		ICardStore lookupStore = DataManager.getInstance().getMagicDBStore();
		if (lookupStore == null) {
			card.setError(ImportError.NO_DB_ERROR);
			return base;
		}
		MagicCard ref = findRef(base, lookupStore);
		if (ref != null) {
			if (originalSet == null || ref.getSet().equals(originalSet)) {
				card.setMagicCard(ref);
				return null;
			} else {
				if (ed == null || ed.isUnknown()) {
					base.setEmptyFromCard(ref);
					card.setError(ImportError.SET_NOT_FOUND_ERROR);
					return card.getBase();
				} else {
					int id = base.getCardId();
					base.setEmptyFromCard(ref);
					String imageUrl = base.getImageUrl();
					if (id == 0) {
						id = base.syntesizeId();
					}
					base.setCardId(id);// cannot use same id because set is new
					base.set(MagicCardField.IMAGE_URL, imageUrl); // but we can use image from prev set
					card.setError(ImportError.NAME_NOT_FOUND_IN_SET_ERROR);
				}
			}
		} else {
			card.setError(ImportError.NAME_NOT_FOUND_IN_DB_ERROR);
		}
		return card.getBase();
	}

	public static MagicCard findRef(MagicCard base, ICardStore lookupStore) {
		// System.err.println("*** LOOKING FOR  " + base);
		MagicCard ref = null;
		// by id
		if (base.getCardId() != 0) {
			ref = (MagicCard) lookupStore.getCard(base.getCardId());
		}
		// by name
		if (ref == null) {
			String name = base.getName();
			String set = base.getSet();
			for (Iterator iterator = lookupStore.iterator(); iterator.hasNext();) {
				MagicCard a = (MagicCard) iterator.next();
				String lname = a.getName();
				if (name != null && name.equalsIgnoreCase(lname)) {
					// System.err.println("*** name candidate " + a);
					if (set != null && set.equals(a.getSet())) {
						ref = a;
						// System.err.println("*** set match " + a);
						break;
					}
					if (ref == null || ref.getCardId() <= a.getCardId()) {
						ref = a;
						// System.err.println("*** set candidate " + a);
					}
				}
			}
		}
		return ref;
	}

	public static class LookupHash {
		private static String ALL = "All";
		private HashMap<String, HashMap<String, List<IMagicCard>>> mapSetNames = new HashMap<String, HashMap<String, List<IMagicCard>>>();
		private Pattern brackers = Pattern.compile("(.*) \\((.*)\\)");

		public LookupHash(ICardStore lookupStore) {
			mapSetNames.put(ALL, new HashMap<String, List<IMagicCard>>());
			if (lookupStore != null)
				for (Iterator iterator = lookupStore.iterator(); iterator.hasNext();) {
					MagicCard a = (MagicCard) iterator.next();
					String lname = a.getName().toLowerCase(Locale.ENGLISH);
					addToLookup(a, lname);
					if (lname.contains("(")) {
						Matcher m = brackers.matcher(lname);
						if (m.matches()) {
							String one = m.group(1);
							String two = m.group(2);
							addToLookup(a, one);
							addToLookup(a, two);
						}
					} else if (lname.length() != lname.getBytes().length) {
						String x = decompose(lname);
						x = x.replaceAll("æ", "ae");
						// System.err.println(lname + "->" + x);
				addToLookup(a, x);
			}
		}
		}

		public void addToLookup(MagicCard a, String lname) {
			addName(lname, a, mapSetNames.get(ALL));
			addSetName(a.getSet(), lname, a);
		}

		private void addName(String lname, MagicCard a, HashMap<String, List<IMagicCard>> map) {
			List<IMagicCard> x = map.get(lname);
			if (x == null) {
				x = new ArrayList<IMagicCard>(1);
				map.put(lname, x);
			}
			x.add(a);
		}

		private void addSetName(String set, String name, MagicCard a) {
			HashMap<String, List<IMagicCard>> setmap = mapSetNames.get(set);
			if (setmap == null) {
				setmap = new HashMap<String, List<IMagicCard>>(50);
				mapSetNames.put(set, setmap);
			}
			addName(name, a, setmap);
		}

		public static String decompose(String s) {
			return java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD).replaceAll(
					"\\p{InCombiningDiacriticalMarks}+", "");
		}

		public List<IMagicCard> getCandidates(String name) {
			return getCandidates(name, ALL);
		}

		public List<IMagicCard> getCandidates(String name, String set) {
			HashMap<String, List<IMagicCard>> map = mapSetNames.get(set);
			if (map == null)
				return Collections.EMPTY_LIST;
			List<IMagicCard> list = map.get(name.toLowerCase(Locale.ENGLISH));
			if (list == null)
				return Collections.EMPTY_LIST;
			return list;
		}
	}

	/**
	 * Finds and associates imported cards with magic db cards. If card not found in db creates new db cards
	 * and adds to newdbrecords
	 */
	public static void performPreImportWithDb(Collection<IMagicCard> result,
			Collection<IMagicCard> newdbrecords, ICardField[] columns) {
		for (Iterator iterator = result.iterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			if (card instanceof MagicCardPhysical) {
				MagicCard oldCard = card.getBase();
				MagicCard newCard = ImportUtils.updateCardReference((MagicCardPhysical) card);
				if (newCard != null) {
					// import int DB
					newdbrecords.add(newCard);
					Object error = ((MagicCardPhysical) card).getError();
					if (error == ImportError.NAME_NOT_FOUND_IN_SET_ERROR
							|| error == ImportError.NAME_NOT_FOUND_IN_DB_ERROR) {
						((MagicCardPhysical) card).setError(null); // clear error on db import
					}
				} else if (oldCard != card.getBase()) {
					// card is updated - merge
					card.getBase().setNonEmptyFromCard(columns, oldCard);
				}
			} else if (card instanceof MagicCard) {
				newdbrecords.add(card);
			}
		}
	}

	public static void validateDbRecords(ArrayList<IMagicCard> newdbrecords, ArrayList<String> lerrors) {
		for (Iterator iterator = newdbrecords.iterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			MagicCard newCard = card.getBase();
			String prefix = TextPrinter.getString(newCard) + ": Cannot import new card into db: ";
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
		ICardStore magicDbHandler = DataManager.getInstance().getMagicDBStore();
		for (Iterator iterator = newdbrecords.iterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			MagicCard newCard = card.getBase();
			magicDbHandler.add(newCard);
		}
		magicDbHandler.getStorage().save();
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
				MagicCard newCard = card.getBase();
				newCard.setSet(corr);
			}
		}
	}

	public static void updateLocation(Collection<IMagicCard> cards, Location location) {
		Location sideboard = location.toSideboard();
		for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
			IMagicCard iMagicCard = (IMagicCard) iterator.next();
			if (iMagicCard instanceof MagicCardPhysical) {
				MagicCardPhysical mcp = (MagicCardPhysical) iMagicCard;
				if (!mcp.isSideboard())
					mcp.setLocation(location);
				else
					mcp.setLocation(sideboard);
			}
		}
	}
}
