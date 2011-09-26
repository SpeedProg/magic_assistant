package com.reflexit.magiccards.core.sync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IStorage;
import com.reflexit.magiccards.core.model.storage.IStorageContainer;

public class UpdateCardsFromWeb {
	public void updateStore(IMagicCard card, Set<ICardField> fieldMaps, String lang, ICardStore magicDb, IProgressMonitor monitor)
			throws IOException {
		ArrayList<IMagicCard> list = new ArrayList<IMagicCard>(1);
		list.add(card);
		if (lang == null)
			lang = card.getLanguage();
		updateStore(list.iterator(), 1, fieldMaps, lang, magicDb, monitor);
	}

	public void updateStore(Iterator<IMagicCard> iter, int size, Set<ICardField> fieldMaps, String lang, ICardStore magicDb,
			IProgressMonitor monitor) throws IOException {
		monitor.beginTask("Loading additional info...", size * 100 + 10);
		IStorage storage = ((IStorageContainer) magicDb).getStorage();
		ParseGathererDetails rulParser = new ParseGathererDetails();
		rulParser.setMagicDb(magicDb);
		ParseGathererBasicInfo linfoParser = new ParseGathererBasicInfo();
		ParseGathererBasicInfo textParser = new ParseGathererBasicInfo();
		ParseGathererCardLanguages langParser = new ParseGathererCardLanguages();
		langParser.setLanguage(lang);
		monitor.worked(5);
		boolean loadText = fieldMaps.contains(MagicCardField.TEXT);
		boolean loadLang = fieldMaps.contains(MagicCardField.LANG);
		boolean loadImage = fieldMaps.contains(MagicCardField.ID);
		// load
		storage.setAutoCommit(false);
		try {
			for (int i = 0; iter.hasNext(); i++) {
				IMagicCard card = iter.next();
				MagicCard magicCard = card.getBase();
				if (monitor.isCanceled())
					return;
				// load individual card
				monitor.subTask("Updating card " + i + " of " + size);
				try {
					rulParser.parseSingleCard(card, fieldMaps, new SubProgressMonitor(monitor, 50));
					if (loadText) {
						textParser.setCard(card);
						textParser.setFilter(fieldMaps);
						textParser.load(new SubProgressMonitor(monitor, 10));
					}
					if (loadLang) {
						langParser.setCardId(card.getCardId());
						langParser.load(new SubProgressMonitor(monitor, 40));
						int langId = langParser.getLangCardId();
						if (langId != 0) {
							MagicCard newMagicCard = magicCard.cloneCard();
							newMagicCard.setCardId(langId);
							newMagicCard.setEnglishCardId(card.getCardId());
							newMagicCard.setLanguage(lang);
							linfoParser.setCard(newMagicCard);
							linfoParser.load(new SubProgressMonitor(monitor, 40));
							if (magicDb.getCard(newMagicCard.getCardId()) == null) {
								magicDb.add(newMagicCard);
								// System.err.println("Added " +
								// newMagicCard.getName());
							}
						}
					}
				} catch (IOException e) {
					Activator.log("Cannot load card " + e.getMessage() + " " + card.getCardId());
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
		} finally {
			storage.setAutoCommit(true);
			storage.save();
			monitor.worked(5);
			monitor.done();
		}
	}
}
