package com.reflexit.magiccards.core.seller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.storage.AbstractFilteredCardStore;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;
import com.reflexit.magiccards.core.model.storage.MemoryCardStore;
import com.reflexit.magiccards.core.model.utils.CountersMap;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.sync.WebUtils;

public class ParseMOTLPrices extends AbstractPriceProvider {
	private String baseURL;
	private HashMap<String, String> setIdMap = new HashMap<String, String>();
	private static ParseMOTLPrices instance = new ParseMOTLPrices();

	public static ParseMOTLPrices getInstance() {
		return instance;
	}

	private ParseMOTLPrices() {
		super("MOTL (Magic Traders)");
		baseURL = "http://classic.magictraders.com/pricelists/current-magic-excel.txt";
		// hardcoded setIdMap
		setIdMap.put("Limited Edition Alpha", "A");
		setIdMap.put("Limited Edition Beta", "B");
		setIdMap.put("Unlimited Edition", "U");
		// setIdMap.put("Alliances", "AI"); // web
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
		setIdMap.put("Nemesis", "NE");
		setIdMap.put("Ninth Edition", "9th");
		setIdMap.put("Planechase", "HOP");
		setIdMap.put("Revised Edition", "RV"); // web
		setIdMap.put("Seventh Edition", "7th");
		setIdMap.put("Shadowmoor", "SHM");
		setIdMap.put("Tenth Edition", "10th");
		setIdMap.put("Urza's Destiny", "UD"); // web
		setIdMap.put("Urza's Legacy", "UL");
		setIdMap.put("Starter 2000", "ST2");
		setIdMap.put("Starter 1999", "ST1");
		setIdMap.put("Portal", "PT");
		setIdMap.put("Portal Three Kingdoms", "P3");
		setIdMap.put("Promo set for Gatherer", "PRE");
		setIdMap.put("Magic 2014 Core Set", "M14");
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
	public Iterable<IMagicCard> updatePrices(Iterable<IMagicCard> iterable, ICoreProgressMonitor monitor)
			throws IOException {
		monitor.beginTask("Loading prices from " + getURL() + " ...", 100);
		try {
			if (updateFromWeb())
				return iterable;
			else
				return null;
		} finally {
			monitor.done();
		}
	}

	public float getPrice(IMagicCard magicCard) {
		if (magicCard == null)
			return -1;
		return priceMap.get(magicCard.getCardId());
	}

	private long lastUpdate;

	public boolean updateFromWeb() throws IOException {
		if (lastUpdate != 0 && System.currentTimeMillis() - lastUpdate < 60 * 1000)
			return false;
		HashMap<String, Float> res;
		res = new HashMap<String, Float>();
		URL url = new URL(baseURL);
		InputStream openStream = WebUtils.openUrl(url);
		BufferedReader st = new BufferedReader(new InputStreamReader(openStream));
		processFile(st, res);
		st.close();
		lastUpdate = System.currentTimeMillis();
		updatePrices(res);
		return true;
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
			try {
				float f = Float.parseFloat(price);
				res.put(name, f);
			} catch (NumberFormatException e) {
				continue;
			}
		}
	}

	private void updatePrices(HashMap<String, Float> res) {
		IDbCardStore<IMagicCard> db = DataManager.getInstance().getMagicDBStore();
		// System.err.println(db.isInitialized());
		HashMap<String, CountersMap> scandmap = new HashMap();
		Editions editions = Editions.getInstance();
		CountersMap allsetmap = new CountersMap();
		for (String xname : res.keySet()) {
			String cname = xname;
			String set = null;
			if (xname.contains("(")) {
				int lset = xname.lastIndexOf("(");
				cname = xname.substring(0, lset).trim();
				set = xname.substring(lset + 1, xname.length() - 1);
			}
			Collection<IMagicCard> candidates = getCandidates(db, cname);
			if (candidates.size() == 0) {
				// MagicLogger.log("MOTL: Not found card in ma db " + cname);
				continue;
			}
			if (set == null) {
				for (IMagicCard mc : candidates) {
					setDbPrice(mc, res.get(xname), getCurrency());
				}
				continue;
			} else {
				boolean found = false;
				for (IMagicCard mc : candidates) {
					String cset = mc.getSet();
					String abbr = setIdMap.get(cset);
					if (abbr == null)
						abbr = editions.getAbbrByName(cset);
					if (abbr == null) {
						MagicLogger.log("No abbreviation for !! " + cset);
					} else if (abbr.equals(set)) {
						setDbPrice(mc, res.get(xname), getCurrency());
						found = true;
						break;
					}
				}
				if (found)
					continue;
				CountersMap xsetmap = scandmap.get(set);
				if (xsetmap == null) {
					scandmap.put(set, xsetmap = new CountersMap());
				}
				// MagicLogger.log("MOTL: Not found set " + set + " for " +
				// cname);
				allsetmap.inc(set);
				if (candidates.size() < 15)
					for (IMagicCard mc : candidates) {
						String cset = mc.getSet();
						xsetmap.inc(cset);
					}
			}
		}
		if (false) {
			for (String missingSet : scandmap.keySet()) {
				CountersMap countersMap = scandmap.get(missingSet);
				String poss = countersMap.maxKey();
				System.err.println("* Not found set " + missingSet + " possible " + poss + " "
						+ countersMap.get(poss) + " of "
						+ allsetmap.get(missingSet));
				for (Iterator iterator = countersMap.keySet().iterator(); iterator.hasNext();) {
					String cset = (String) iterator.next();
					System.err.println("  possible sets " + cset + " " + editions.getAbbrByName(cset) + " "
							+ countersMap.get(cset));
				}
			}
		}
	}

	protected Collection<IMagicCard> getCandidates(IDbCardStore<IMagicCard> db, String cname) {
		Collection<IMagicCard> candidates = db.getCandidates(cname);
		if (candidates.size() == 0) {
			String ccname = correctName(cname);
			candidates = db.getCandidates(ccname);
		}
		return candidates;
	}

	private String correctName(String cname) {
		if (cname.contains("AE")) {
			cname = cname.replaceAll("AE", "Æ");
		}
		int sl = cname.indexOf('/');
		if (sl >= 0) {
			cname = cname.replaceFirst("/", " // ");
			cname += " (" + cname.substring(0, sl) + ")";
		}
		return cname;
	}

	@Override
	public URL buy(Iterable<IMagicCard> cards) {
		String url = "http://magictraders.crystalcommerce.com/products/multi_search";
		String res = export(cards);
		System.setProperty("clipboard", res);
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			MagicLogger.log(e);
			return null;
		}
	}

	public static void main(String[] args) throws IOException {
		ParseMOTLPrices prices = new ParseMOTLPrices();
		AbstractFilteredCardStore<IMagicCard> fstore = new AbstractFilteredCardStore<>(
				new MemoryCardStore<IMagicCard>());
		MagicCard card = new MagicCard();
		card.setSet("Time Spiral");
		card.setName("Amrou Scout");
		MagicCard card2 = new MagicCard();
		card2.setSet("Magic 2011");
		card2.setName("AEther Adept");
		fstore.getCardStore().add(card);
		fstore.getCardStore().add(card2);
		fstore.update();
		prices.updatePrices(fstore, ICoreProgressMonitor.NONE);
		System.err.println(card2.getName() + " " + card2.getDbPrice());
	}
}
