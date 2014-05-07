package com.reflexit.magiccards.core.seller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Editions.Edition;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;
import com.reflexit.magiccards.core.model.storage.MemoryFilteredCardStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.sync.WebUtils;

public class ParseTcgPlayerPrices extends AbstractPriceProvider {
	public final String PARTNER_KEY = "MGCASSTNT";

	public static enum Type {
		Low("lowprice"),
		Medium("avgprice"),
		High("hiprice"), ;
		String tag;

		Type(String tag) {
			this.tag = tag;
		}
	}

	private static ParseTcgPlayerPrices[] providers = new ParseTcgPlayerPrices[Type.values().length];
	static {
		for (Type type : Type.values()) {
			providers[type.ordinal()] = new ParseTcgPlayerPrices(type);
		}
	}
	private Type type;

	private ParseTcgPlayerPrices() {
		this(Type.Medium);
	}

	private ParseTcgPlayerPrices(Type type) {
		super(null);
		this.type = type;
		name = "TCG Player (" + type.name() + ")";
	}

	private static HashMap<String, String> setMap;
	static {
		setMap = new HashMap<String, String>();
		Editions ed = Editions.getInstance();
		ed.getEditions();
		for (Iterator<Edition> iterator = ed.getEditions().iterator(); iterator.hasNext();) {
			Edition set = iterator.next();
			String name = set.getName();
			if (name.startsWith("Magic 20")) {
				String name2 = name.replaceFirst(" Core Set", "");
				String year = name2.substring(8, 10);
				setMap.put(name, name2 + " (M" + year + ")");
			}
		}
		setMap.put("Limited Edition Alpha", "Alpha Edition");
		setMap.put("Limited Edition Beta", "Beta Edition");
		// setMap.put("Classic Sixth Edition", "Sixth Edition");
		setMap.put("Time Spiral \"Timeshifted\"", "Timeshifted");
		setMap.put("Magic: The Gathering-Commander", "Commander");
		setMap.put("Seventh Edition", "7th Edition");
		setMap.put("Eighth Edition", "8th Edition");
		setMap.put("Ninth Edition", "9th Edition");
		setMap.put("Tenth Edition", "10th Edition");
		setMap.put("Planechase 2012 Edition", "Planechase 2012");
		setMap.put("Duel Decks: Knights vs. Dragons", "Duel Decks: Knights vs Dragons ");
		setMap.put("Ravnica: City of Guilds", "Ravnica");
		setMap.put("Commander 2013 Edition", "Commander 2013");
	}

	@Override
	public Iterable<IMagicCard> updatePrices(Iterable<IMagicCard> iterable, ICoreProgressMonitor monitor) throws IOException {
		int size = getSize(iterable);
		monitor.beginTask("Loading prices from " + getURL() + " ...", size + 10);
		IDbCardStore db = DataManager.getCardHandler().getMagicDBStore();
		monitor.worked(5);
		try {
			for (IMagicCard magicCard : iterable) {
				if (monitor.isCanceled())
					return null;
				float price = getPrice(magicCard);
				if (price < 0) {
					int id = magicCard.getFlipId();
					IMagicCard flipCard = (IMagicCard) db.getCard(id);
					if (flipCard != null)
						price = getPrice(flipCard);
				}
				if (price > 0) {
					setDbPrice(magicCard, price);
				}
				monitor.worked(1);
			}
		} finally {
			monitor.done();
		}
		return iterable;
	}

	public float getPrice(IMagicCard magicCard) {
		BufferedReader st = null;
		try {
			URL url = createCardUrl(magicCard);
			InputStream openStream = WebUtils.openUrl(url);
			st = new BufferedReader(new InputStreamReader(openStream));
			String xml = FileUtils.readFileAsString(st);
			float price = parsePrice(xml);
			if (price == -1) {
				MagicLogger.log("Failed to load price for " + url);
			}
			return price;
		} catch (Exception e) {
			MagicLogger.log(e);
			return -1;
		} finally {
			try {
				if (st != null)
					st.close();
			} catch (IOException e) {
				// screw it
			}
		}
	}

	private float parsePrice(String xml) {
		// <avgprice>0.14</avgprice>
		String pricetag = type.tag;
		try {
			int i = xml.indexOf(pricetag);
			if (i == -1) {
				return -1; // hmm not found
			}
			int j = xml.indexOf(pricetag, i + 1);
			String pr = xml.substring(i + 12, j - 5);
			return Float.parseFloat(pr);
		} catch (Exception e) {
			return -1;
		}
	}

	private URL createCardUrl(IMagicCard magicCard) throws MalformedURLException {
		String setm = magicCard.getSet();
		String set = setMap.get(setm);
		if (set == null)
			set = setm;
		String name = magicCard.getName();
		name = name.replaceAll("Æ", "AE");
		name = name.replaceAll("ö", "o");
		name = name.replaceAll(" \\(.*$", "");
		String url = "http://partner.tcgplayer.com/x/phl.asmx/p?pk=" + PARTNER_KEY + "&s=" + set + "&p=" + name;
		url = url.replaceAll(" ", "+");
		url = url.replaceAll("'", "%27");
		return new URL(url);
	}

	@Override
	public URL getURL() {
		try {
			return new URL("http://www.tcgplayer.com");
		} catch (MalformedURLException e) {
			return null;
		}
	}

	@Override
	public URL buy(Iterable<IMagicCard> cards) {
		String url = "http://store.tcgplayer.com/list/selectproductmagic.aspx?partner=" + PARTNER_KEY;
		String res = export(cards);
		if (res.length() < 1600) {
			res = res.replaceAll("\r", "");
			url += "&c=" + res.replaceAll("\n", "||");
		} else {
			System.setProperty("clipboard", res);
		}
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			MagicLogger.log(e);
			return null;
		}
	}

	public static void main(String[] args) {
		MagicCard card = new MagicCard();
		card.setName("Flameborn Viron");
		card.setSet("New Phyrexia");
		ParseTcgPlayerPrices pp = new ParseTcgPlayerPrices();
		float price = pp.getPrice(card);
		System.err.println("Price for " + card + " " + price);
		MagicCard card1 = new MagicCard();
		card1.setName("Coat of Arms");
		card1.setSet("Magic 2010");
		float price1 = pp.getPrice(card1);
		System.err.println("Price for " + card1 + " " + price1);
		MemoryFilteredCardStore<IMagicCard> store = new MemoryFilteredCardStore<IMagicCard>();
		ArrayList<IMagicCard> list = new ArrayList<IMagicCard>();
		list.add(card);
		list.add(card1);
		store.addAll(list);
		store.update();
		pp.buy(store);
	}

	public static IPriceProvider create(Type type) {
		return providers[type.ordinal()];
	}
}