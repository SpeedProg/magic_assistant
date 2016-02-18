package com.reflexit.magiccards.core.sync;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IStorage;
import com.reflexit.magiccards.core.model.storage.IStorageContainer;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.monitor.SubCoreProgressMonitor;

public class UpdateCardsFromWeb {
	public static final String UPDATE_BASIC_LAND_PRINTINGS = "land";
	public static final String UPDATE_OTHER_PRINTINGS = "other.printings";
	public static final String UPDATE_LANGUAGE = "lang";
	public static final String UPDATE_SPECIAL = "special";

	public void updateStore(IMagicCard card, Set<ICardField> fieldMaps, String lang, ICardStore magicDb,
			ICoreProgressMonitor monitor)
			throws IOException {
		ArrayList<IMagicCard> list = new ArrayList<IMagicCard>(1);
		list.add(card);
		if (lang == null)
			lang = card.getLanguage();
		updateStore(list.iterator(), 1, fieldMaps, lang, magicDb, monitor);
	}

	public void updateStore(Iterator<IMagicCard> iter, int size, Set<ICardField> fieldMaps, String lang,
			ICardStore magicDb,
			ICoreProgressMonitor monitor) throws IOException {
		monitor.beginTask("Loading additional info...", size * 150 + 10);
		IStorage storage = ((IStorageContainer) magicDb).getStorage();
		ParseGathererOracle oracleParser = new ParseGathererOracle();
		oracleParser.setMagicDb(magicDb);
		ParseGathererPrinted linfoParser = new ParseGathererPrinted();
		ParseGathererPrinted printedParser = new ParseGathererPrinted();
		ParseGathererCardLanguages langParser = new ParseGathererCardLanguages();
		ParseGathererLegality legParser = new ParseGathererLegality();
		langParser.setLanguage(lang);
		monitor.worked(5);
		boolean loadText = fieldMaps.contains(MagicCardField.TEXT);
		boolean loadLang = fieldMaps.contains(MagicCardField.LANG);
		boolean loadImage = fieldMaps.contains(MagicCardField.ID);
		boolean loadLegality = fieldMaps.contains(MagicCardField.LEGALITY);
		if (loadText) {
			printedParser.addFilter(MagicCardField.TEXT);
		}
		if (loadLang) {
			if (lang == null || lang.isEmpty() || lang.equals("English"))
				loadLang = false;
		}
		boolean localized = false;
		// load
		storage.setAutoCommit(false);
		try {
			int failedLangUpdate = 0;
			for (int i = 0; iter.hasNext(); i++) {
				if (monitor.isCanceled())
					return;
				IMagicCard card = iter.next();
				IMagicCard magicCard = card.getBase();
				if (magicCard == null) continue;
				if (magicCard.getEnglishCardId() != 0) {
					// localized
					localized = true;
					if (fieldMaps.contains(MagicCardField.NAME))
						printedParser.addFilter(MagicCardField.NAME);
					if (fieldMaps.contains(MagicCardField.TYPE))
						printedParser.addFilter(MagicCardField.TYPE);
				} else {
					localized = false;
				}
				// load individual card
				monitor.subTask("Updating card " + i + " of " + size);
				int cardId = card.getGathererId();
				try {
					oracleParser.parseSingleCard(card, fieldMaps, new SubCoreProgressMonitor(monitor, 50));
					if (loadText || localized) {
						printedParser.setCard(card);
						printedParser.load(new SubCoreProgressMonitor(monitor, 10));
					}
					if (loadLegality && magicCard instanceof MagicCard) {
						synchronized (legParser) {
							legParser.setCard((MagicCard) magicCard);
							legParser.load(new SubCoreProgressMonitor(monitor, 10));
						}
					}
					if (loadLang) {
						langParser.setCardId(cardId);
						langParser.load(new SubCoreProgressMonitor(monitor, 40));
						List<Integer> list = langParser.getLangCardIds();
						if (list.size() > 0) {
							for (Integer integer : list) {
								int langId = integer;
								MagicCard newMagicCard = (MagicCard) magicCard.cloneCard();
								newMagicCard.setCardId(langId);
								int englishCardId = card.getEnglishCardId();
								if (englishCardId == 0)
									englishCardId = cardId;
								newMagicCard.setEnglishCardId(englishCardId);
								newMagicCard.setLanguage(lang);
								linfoParser.setCard(newMagicCard);
								linfoParser.load(new SubCoreProgressMonitor(monitor, 40));
								if (magicDb.getCard(newMagicCard.getCardId()) == null) {
									magicDb.add(newMagicCard);
									// System.err.println("Added " +
									// newMagicCard.getName());
								}
							}

						} else {
							MagicLogger.log("Cannot load " + lang + " of " + cardId);
							monitor.worked(40);
							failedLangUpdate++;
						}
					}
				} catch (IOException e) {
					MagicLogger.log("Cannot load card " + e.getMessage() + " " + cardId);
				}
				if (monitor.isCanceled())
					return;
				magicDb.update(magicCard, fieldMaps);
				if (loadImage) {
					// load and cache image offline
					CardCache.loadCardImageOffline(card, false);
				}
				monitor.worked(1);
			}
			if (failedLangUpdate > 0) {
				throw new MagicException("Localized version for " + lang + " for "
						+ failedLangUpdate + " cards is not available");
			}
		} finally {
			storage.setAutoCommit(true);
			monitor.worked(5);
			monitor.done();
		}
	}

	public static void downloadUpdates(String set, String toFile, Properties options, ICoreProgressMonitor pm)
			throws FileNotFoundException, MalformedURLException, IOException {
		PrintStream out = System.out;
		if (toFile != null)
			out = new PrintStream(new FileOutputStream(new File(toFile)), true, FileUtils.UTF8);
		TextPrinter.printHeader(out);
		String land = (String) options.get(UpdateCardsFromWeb.UPDATE_BASIC_LAND_PRINTINGS);
		final boolean bland = "true".equals(land);
		String other = (String) options.get(UpdateCardsFromWeb.UPDATE_OTHER_PRINTINGS);
		final boolean bother = "true".equals(other);
		final GatherHelper.ILoadCardHander handler2 = new GatherHelper.OutputHandler(out, bland, bother);
		GatherHelper.StashLoadHandler handler = new GatherHelper.StashLoadHandler() {
			LinkedHashMap<Integer, MagicCard> cards = new LinkedHashMap<Integer, MagicCard>();

			@Override
			public void handleCard(MagicCard card) {
				int cardId = card.getCardId();
				Integer id = Integer.valueOf(cardId);
				MagicCard prev = cards.get(id);
				if (prev != null) {
					if (prev.getCollNumber().length() > 0
							&& !prev.getCollNumber().equals(card.getCollNumber())) {
						// land cards have mismatching id link in checklist
						prev.setCollNumber("x");
						prev.setArtist(null);
						return; // bug in gatherer
					}
					// merge with info from checklist
					prev.setArtist(card.getArtist());
					prev.setCollNumber(card.getCollNumber());
				} else {
					if (card.getName().equals("Mountain")) {
						card.setText("R");
						card.setOracleText("R");
					}
					cards.put(id, card);
				}
			}

			@Override
			public Collection<MagicCard> getPrimary() {
				return cards.values();
			}

			@Override
			public void handleSecondary(MagicCard primary, MagicCard secondary) {
				if (bland && primary.getSet() != null && primary.getSet().equals(secondary.getSet())) {
					handleCard(secondary);
				} else if (bother) {
					handleCard(secondary);
				}
			}
		};
		pm.beginTask("Downloading...", 15000);
		try {
			new ParseGathererSearchStandard().loadSet(set, handler, new SubCoreProgressMonitor(pm, 5000,
					SubCoreProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
			new ParseGathererSearchChecklist().loadSet(set, handler, new SubCoreProgressMonitor(pm, 5000,
					SubCoreProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
			Set<ICardField> fieldMap = new HashSet<ICardField>();
			fieldMap.add(MagicCardField.COLLNUM);
			fieldMap.add(MagicCardField.ARTIST);
			SubCoreProgressMonitor pm2 = new SubCoreProgressMonitor(pm, 5000,
					SubCoreProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
			int n = handler.getPrimary().size();
			pm2.beginTask("Updating", n);
			int i = 0;
			for (Iterator<MagicCard> iterator = handler.getPrimary().iterator(); iterator.hasNext(); i++) {
				MagicCard c = iterator.next();
				if (c.getCollNumber().equals("x")) {
					pm2.subTask(c.toString() + " (" + i + " of " + n + ")");
					new ParseGathererOracle().updateCard(c, fieldMap, ICoreProgressMonitor.NONE);
				}
				handler2.handleCard(c);
				pm2.worked(1);
				if (pm2.isCanceled() || pm.isCanceled())
					break;
			}
			pm2.done();
		} finally {
			out.close();
			pm.done();
		}
	}
}
