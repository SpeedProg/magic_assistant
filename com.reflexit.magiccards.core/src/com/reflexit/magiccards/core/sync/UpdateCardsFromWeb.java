package com.reflexit.magiccards.core.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Set;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
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

	public void updateStore(IMagicCard card, Set<ICardField> fieldMaps, String lang, ICardStore magicDb, ICoreProgressMonitor monitor)
			throws IOException {
		ArrayList<IMagicCard> list = new ArrayList<IMagicCard>(1);
		list.add(card);
		if (lang == null)
			lang = card.getLanguage();
		updateStore(list.iterator(), 1, fieldMaps, lang, magicDb, monitor);
	}

	public void updateStore(Iterator<IMagicCard> iter, int size, Set<ICardField> fieldMaps, String lang, ICardStore magicDb,
			ICoreProgressMonitor monitor) throws IOException {
		monitor.beginTask("Loading additional info...", size * 100 + 10);
		IStorage storage = ((IStorageContainer) magicDb).getStorage();
		ParseGathererDetails oracleParser = new ParseGathererDetails();
		oracleParser.setMagicDb(magicDb);
		ParseGathererBasicInfo linfoParser = new ParseGathererBasicInfo();
		ParseGathererBasicInfo printedParser = new ParseGathererBasicInfo();
		ParseGathererCardLanguages langParser = new ParseGathererCardLanguages();
		langParser.setLanguage(lang);
		monitor.worked(5);
		boolean loadText = fieldMaps.contains(MagicCardField.TEXT);
		boolean loadLang = fieldMaps.contains(MagicCardField.LANG);
		boolean loadImage = fieldMaps.contains(MagicCardField.ID);
		if (loadText) {
			printedParser.addFilter(MagicCardField.TEXT);
		}
		boolean localized = false;
		// load
		storage.setAutoCommit(false);
		try {
			int failedLangUpdate = 0;
			for (int i = 0; iter.hasNext(); i++) {
				IMagicCard card = iter.next();
				IMagicCard magicCard = card.getBase();
				if (monitor.isCanceled())
					return;
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
				try {
					oracleParser.parseSingleCard(card, fieldMaps, new SubCoreProgressMonitor(monitor, 50));
					if (loadText || localized) {
						printedParser.setCard(card);
						printedParser.load(new SubCoreProgressMonitor(monitor, 10));
					}
					if (loadLang) {
						langParser.setCardId(card.getCardId());
						langParser.load(new SubCoreProgressMonitor(monitor, 40));
						int langId = langParser.getLangCardId();
						if (langId != 0) {
							MagicCard newMagicCard = (MagicCard) magicCard.cloneCard();
							newMagicCard.setCardId(langId);
							newMagicCard.setEnglishCardId(card.getCardId());
							newMagicCard.setLanguage(lang);
							linfoParser.setCard(newMagicCard);
							linfoParser.load(new SubCoreProgressMonitor(monitor, 40));
							if (magicDb.getCard(newMagicCard.getCardId()) == null) {
								magicDb.add(newMagicCard);
								// System.err.println("Added " +
								// newMagicCard.getName());
							}
						} else {
							failedLangUpdate++;
						}
					}
				} catch (IOException e) {
					MagicLogger.log("Cannot load card " + e.getMessage() + " " + card.getCardId());
				}
				if (monitor.isCanceled())
					return;
				magicDb.update(magicCard);
				if (loadImage) {
					// load and cache image offline
					CardCache.loadCardImageOffline(card, false);
				}
				monitor.worked(1);
			}
			if (failedLangUpdate > 0) {
				throw new MagicException("Localized version for " + lang + " for " + failedLangUpdate + " cards is not available");
			}
		} finally {
			storage.setAutoCommit(true);
			storage.save();
			monitor.worked(5);
			monitor.done();
		}
	}

	public static BufferedReader openUrlReader(URL url) throws IOException {
		InputStream openStream = UpdateCardsFromWeb.openUrl(url);
		BufferedReader st = new BufferedReader(new InputStreamReader(openStream, FileUtils.CHARSET_UTF_8));
		return st;
	}

	public static InputStream openUrl(URL url) throws IOException {
		IOException rt = null;
		for (int i = 0; i < 3; i++) {
			// 3 attempts
			try {
				HttpURLConnection huc = (HttpURLConnection) url.openConnection();
				huc.setConnectTimeout(30 * 1000);
				huc.setReadTimeout(30 * 1000);
				huc.connect();
				InputStream openStream = huc.getInputStream();
				return openStream;
			} catch (IOException e) {
				MagicLogger.log("Connection error on url " + url + ": " + e.getMessage() + ". Attempt " + i);
				rt = e;
				continue;
			}
		}
		if (rt != null) {
			MagicLogger.log("Connection error on url " + url + ": " + rt.getMessage() + ". Giving up");
			throw rt;
		}
		throw new RuntimeException("Not possible");
	}

	public static void downloadUpdates(String set, String toFile, Properties options, ICoreProgressMonitor pm)
			throws FileNotFoundException, MalformedURLException, IOException {
		PrintStream out = System.out;
		if (toFile != null)
			out = new PrintStream(new FileOutputStream(new File(toFile)), true, FileUtils.UTF8);
		TextPrinter.printHeader(IMagicCard.DEFAULT, out);
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
					if (prev.getCollNumber().length() > 0)
						return; // bug in gatherer
					// merge with info from checklist
					prev.setArtist(card.getArtist());
					prev.setCollNumber(card.getCollNumber());
				} else {
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
		pm.beginTask("Downloading...", 10000);
		try {
			new ParseGathererSearchStandard().loadSet(set, handler, new SubCoreProgressMonitor(pm, 5000,
					SubCoreProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
			new ParseGathererSearchChecklist().loadSet(set, handler, new SubCoreProgressMonitor(pm, 5000,
					SubCoreProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
			for (Iterator<MagicCard> iterator = handler.getPrimary().iterator(); iterator.hasNext();) {
				MagicCard c = iterator.next();
				handler2.handleCard(c);
			}
		} finally {
			out.close();
			pm.done();
		}
	}
}
