package com.reflexit.magiccards.core.seller;

import gnu.trove.map.TIntFloatMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.MemoryCardStorage;
import com.reflexit.magiccards.core.model.storage.MemoryCardStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.sync.UpdateCardsFromWeb;

public class ParseMOTLPrices extends AbstractPriceProvider {
	private String baseURL;
	private HashMap<String, String> setIdMap = new HashMap<String, String>();

	public ParseMOTLPrices() {
		super("MOTL (Magic Traders)");
		baseURL = "http://classic.magictraders.com/pricelists/current-magic-excel.txt";
		// hardcoded setIdMap
		setIdMap.put("Limited Edition Alpha", "A");
		setIdMap.put("Limited Edition Beta", "B");
		setIdMap.put("Unlimited Edition", "U");
		setIdMap.put("Alliances", "AI"); // web
		setIdMap.put("Archenemy", "ARC");
		setIdMap.put("Battle Royale Box Set", "BR");
		setIdMap.put("Beatdown Box Set", "BD");
		setIdMap.put("Chronicles", "CH");
		setIdMap.put("Classic Sixth Edition", "6th");
		setIdMap.put("Conflux", "CFX"); // web
		setIdMap.put("Eighth Edition", "8th");
		setIdMap.put("Fifth Edition", "5th");
		setIdMap.put("Fourth Edition", "4th");
		setIdMap.put("Homelands", "HL"); // web
		setIdMap.put("Nemesis", "NM"); // web
		setIdMap.put("Ninth Edition", "9th");
		setIdMap.put("Planechase", "HOP");
		setIdMap.put("Revised Edition", "RV"); // web
		setIdMap.put("Seventh Edition", "7th");
		setIdMap.put("Shadowmoor", "SHM");
		setIdMap.put("Tenth Edition", "10th");
		setIdMap.put("Urza's Destiny", "UD"); // web
		setIdMap.put("Urza's Legacy", "UY"); // web
	}

	@Override
	public URL getURL() {
		try {
			return new URL("http://magictraders.com");
		} catch (MalformedURLException e) {
			return null;
		}
	}

	@Override
	public void updatePrices(Iterable<IMagicCard> iterable, ICoreProgressMonitor monitor) throws IOException {
		int size = 0;
		for (IMagicCard magicCard : iterable) {
			size++;
		}
		monitor.beginTask("Loading prices from " + getURL() + " ...", size + 10);
		TIntFloatMap priceMap = DataManager.getDBPriceStore().getPriceMap(this);
		HashMap<String, Float> prices = parse();
		monitor.worked(5);
		Editions editions = Editions.getInstance();
		IDbCardStore db = DataManager.getCardHandler().getMagicDBStore();
		try {
			for (IMagicCard magicCard : iterable) {
				if (monitor.isCanceled())
					return;
				float price = getPrice(prices, editions, magicCard);
				if (price < 0) {
					int id = magicCard.getFlipId();
					IMagicCard flipCard = (IMagicCard) db.getCard(id);
					if (flipCard != null)
						price = getPrice(prices, editions, flipCard);
				}
				if (price > 0) {
					// if (!setIdMap.containsKey(set))
					// setIdMap.put(set, id);
					priceMap.put(magicCard.getCardId(), price);
				}
				monitor.worked(1);
			}
		} finally {
			monitor.done();
		}
	}

	public float getPrice(HashMap<String, Float> prices, Editions editions, IMagicCard magicCard) {
		if (magicCard == null)
			return -1;
		String set = magicCard.getSet();
		String abbr = setIdMap.get(set);
		if (abbr == null)
			abbr = editions.getAbbrByName(set);
		String nameset = magicCard.getName() + " (" + abbr + ")";
		float price = -1;
		Float price1 = prices.get(nameset);
		if (price1 != null) {
			price = (price1.floatValue());
		} else {
			price1 = prices.get(magicCard.getName());
			if (price1 != null)
				price = (price1.floatValue());
		}
		// if (price1 == null) {
		// System.err.println("Cannot find price for " + magicCard + " abbr " + abbr + " " +
		// magicCard.getSet());
		// }
		return price;
	}

	public void updateStore(IFilteredCardStore<IMagicCard> fstore, ICoreProgressMonitor monitor) throws IOException {
		updatePrices(fstore, monitor);
	}

	public HashMap<String, Float> parse() throws IOException {
		HashMap<String, Float> res = new HashMap<String, Float>();
		URL url = new URL(baseURL);
		InputStream openStream = UpdateCardsFromWeb.openUrl(url);
		BufferedReader st = new BufferedReader(new InputStreamReader(openStream));
		processFile(st, res);
		st.close();
		return res;
	}

	/*-
	 * AEther Adept (M11),              0.40,  0.00,   0.40,   0.40,   0.40,  0.00,   1
	 */
	private void processFile(BufferedReader st, HashMap<String, Float> res) throws IOException {
		String line = "";
		while ((line = st.readLine()) != null) {
			String[] fields = line.split("\\|");
			if (fields.length < 8)
				continue;
			String name = fields[0].trim();
			String price = fields[1].trim();
			if (name.contains("AE")) {
				name = name.replaceAll("AE", "Æ");
			}
			try {
				float f = Float.parseFloat(price);
				res.put(name, f);
			} catch (NumberFormatException e) {
				continue;
			}
		}
	}

	public static void main(String[] args) throws IOException {
		ParseMOTLPrices prices = new ParseMOTLPrices();
		AbstractFilteredCardStore<IMagicCard> fstore = new AbstractFilteredCardStore<IMagicCard>() {
			MemoryCardStore<MemoryCardStorage<IMagicCard>> store = new MemoryCardStore<MemoryCardStorage<IMagicCard>>();

			public ICardStore getCardStore() {
				return store;
			}
		};
		MagicCard card = new MagicCard();
		card.setSet("Time Spiral");
		card.setName("Amrou Scout");
		MagicCard card2 = new MagicCard();
		card2.setSet("Magic 2011");
		card2.setName("AEther Adept");
		fstore.getCardStore().add(card);
		fstore.getCardStore().add(card2);
		fstore.update();
		prices.updateStore(fstore, ICoreProgressMonitor.NONE);
		System.err.println(card2.getName() + " " + card2.getDbPrice());
	}
}
