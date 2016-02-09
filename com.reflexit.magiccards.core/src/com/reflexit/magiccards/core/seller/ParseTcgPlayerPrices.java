package com.reflexit.magiccards.core.seller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.OfflineException;
import com.reflexit.magiccards.core.exports.HtmlTableImportDelegate;
import com.reflexit.magiccards.core.exports.ImportData;
import com.reflexit.magiccards.core.exports.ImportSource;
import com.reflexit.magiccards.core.exports.ImportUtils;
import com.reflexit.magiccards.core.model.Edition;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.CardList;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.sync.WebUtils;

public class ParseTcgPlayerPrices extends AbstractPriceProvider {
	public final String PARTNER_KEY = "MGCASSTNT";

	// http://magic.tcgplayer.com/db/price_guide.asp?setname=From%20the%20Vault:%20Twenty
	public static enum Type {
		Low("lowprice"), Medium("avgprice"), High("hiprice"),;
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

	static synchronized void initSetMap() {
		if (setMap == null) {
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
				} else if (name.startsWith("Premium Deck Series")) {
					setMap.put(name, name.replace("Premium Deck Series", "PDS"));
				}
			}
			setMap.put("Duel Decks: Knights vs. Dragons", "Duel Decks: Knights vs Dragons ");
			setMap.put("Tenth Edition", "10th Edition");
		}
	}

	public static Map<String, String> getSetAliasesMap() {
		initSetMap();
		return setMap;
	}

	@Override
	public Iterable<IMagicCard> updatePrices(Iterable<IMagicCard> iterable, ICoreProgressMonitor monitor)
			throws IOException {
		if (WebUtils.isWorkOffline())
			throw new OfflineException();
		CardList list = new CardList(iterable);
		int size = list.size();
		Set<Object> uniqueSets = list.getUnique(MagicCardField.SET);
		monitor.beginTask("Loading prices from " + getURL() + " ...", size + 10 + uniqueSets.size());
		try {
			IDbCardStore db = DataManager.getCardHandler().getMagicDBStore();
			int processedSets = 0;
			for (Object object : uniqueSets) {
				try {
					String set = (String) object;
					if (monitor.isCanceled())
						return iterable;
					Map<MagicCard, Float> map = getSetPrices(set);
					if (map.size() != 0) {
						processedSets++;
						for (MagicCard magicCard : map.keySet()) {
							float price = map.get(magicCard);
							ImportUtils.getFixedName(magicCard);// fix Aether
							MagicCard ref = ImportUtils.findRef(magicCard, db);
							if (ref != null) {
								if (price == 0)
									price = -0.0001f;
								setDbPrice(ref, price, getCurrency());
							}
						}
					}
				} catch (Exception e) {
					MagicLogger.log(e);
				}
				monitor.worked(1);
			}
			monitor.worked(5);
			if (processedSets != uniqueSets.size()) {
				for (IMagicCard magicCard : iterable) {
					try {
						if (monitor.isCanceled())
							return iterable;
						float price = getPrice(magicCard);
						if (price < 0) {
							int id = magicCard.getFlipId();
							IMagicCard flipCard = (IMagicCard) db.getCard(id);
							if (flipCard != null)
								price = getPrice(flipCard);
						}
						// if (price > 0)
						{
							if (price == 0)
								price = -0.0001f;
							setDbPrice(magicCard, price, getCurrency());
						}
						monitor.worked(1);
					} catch (Exception e) {
						MagicLogger.log(e);
					}
				}
			}
		} finally {
			monitor.done();
		}
		return iterable;
	}

	private Map<MagicCard, Float> getSetPrices(String origset) {
		getSetAliasesMap();
		final HashMap<MagicCard, Float> res = new HashMap<MagicCard, Float>();
		try {
			HtmlTableImportDelegate delegate = new HtmlTableImportDelegate() {
				@Override
				protected ICardField getFieldByName(String hd) {
					ICardField fieldByName = super.getFieldByName(hd);
					if (fieldByName == null) {
						hd = hd.toUpperCase();
						if ((hd.equals("MID") || hd.equals("MED")) && type == Type.Medium)
							return MagicCardField.DBPRICE;
						if (hd.equals("LOW") && type == Type.Low)
							return MagicCardField.DBPRICE;
						if (hd.equals("HIGH") && type == Type.High)
							return MagicCardField.DBPRICE;
					}
					return fieldByName;
				}

				@Override
				public void setFieldValue(MagicCardPhysical card, ICardField field, int i, String value) {
					if (field == MagicCardField.DBPRICE) {
						value = value.replace("$", "");
						res.put(card.getBase(), Float.valueOf(value));
					} else
						super.setFieldValue(card, field, i, value);
				}
			};
			Collection<String> trysets = getSetOptions(origset);
			for (String set : trysets) {
				String setE = URLEncoder.encode(set, "UTF-8");
				URL url = new URL("http://magic.tcgplayer.com/db/search_result.asp?Set_Name=" + setE);
				setMap.put(origset, set);
				try {
					ImportData importData = new ImportData();
					importData.setImportSource(ImportSource.URL);
					importData.setProperty(ImportSource.URL.name(), url.toExternalForm());
					importData.setText(WebUtils.openUrlText(url));
					delegate.init(importData);
					delegate.run(ICoreProgressMonitor.NONE);
					break;
				} catch (IOException e) {
					continue;
				}
			}
		} catch (Exception e) {
			MagicLogger.log(e);
		}
		return res;
	}

	public float getPrice(IMagicCard magicCard) {
		try {
			getSetAliasesMap();
			float price = -1;
			String origset = magicCard.getSet();
			Collection<String> trysets = getSetOptions(origset);
			for (Iterator iterator = trysets.iterator(); iterator.hasNext();) {
				String altset = (String) iterator.next();
				URL url = createCardUrl(magicCard, altset);
				try {
					String xml = WebUtils.openUrlText(url, 1);
					price = parsePrice(xml);
				} catch (Exception e) {
					MagicLogger.log("Failed to load price for " + url + ": " + e.getLocalizedMessage());
					continue;
				}
				if (price < 0) {
					MagicLogger.log("Failed to load price for " + url);
				} else {
					if (!origset.equals(altset)) {
						setMap.put(origset, altset);
					}
					return price;
				}
			}
			return price;
		} catch (Exception e) {
			MagicLogger.log(e);
			return -4;
		}
	}

	private Collection<String> getSetOptions(String setm) {
		ArrayList<String> res = new ArrayList<String>();
		String set = getSetAliasesMap().get(setm);
		if (set != null) {
			res.add(set);
		}
		res.add(setm);
		Edition edition = Editions.getInstance().getEditionByName(setm);
		String[] aliases = edition.getAliases();
		for (int i = 0; i < aliases.length; i++) {
			String altset = aliases[i];
			res.add(altset);
		}
		return res;
	}

	private float parsePrice(String xml) {
		// <avgprice>0.14</avgprice>
		String pricetag = type.tag;
		try {
			int i = xml.indexOf(pricetag);
			if (i == -1) {
				return -3; // hmm not found
			}
			int j = xml.indexOf("<", i + 1);
			String pr = xml.substring(i + pricetag.length() + 1, j);
			return Float.parseFloat(pr);
		} catch (Exception e) {
			return -2;
		}
	}

	private URL createCardUrl(IMagicCard magicCard, String set) throws MalformedURLException {
		String name = magicCard.getName();
		name = name.replaceAll("Æ", "AE");
		name = name.replaceAll("ö", "o");
		name = name.replaceAll(" \\(.*$", "");
		String url = "http://partner.tcgplayer.com/x3/phl.asmx/p?v=3&pk=" + PARTNER_KEY + "&s=" + set + "&p=" + name;
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
		DataManager.getInstance().syncInitDb();
		ParseTcgPlayerPrices pp = new ParseTcgPlayerPrices();
		pp.getPrice("Flameborn Viron", "New Phyrexia");
		pp.getPrice("Coat of Arms", "Magic 2010");
		List singletonList = Collections.singletonList(mc("Akroma's Vengeance", "From the Vault: Twenty"));
		try {
			pp.updatePrices(singletonList, ICoreProgressMonitor.NONE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	float getPrice(String name, String set) {
		MagicCard card = mc(name, set);
		float price = getPrice(card);
		// System.err.println("Price for " + card + " " + price);
		return price;
	}

	private static MagicCard mc(String name, String set) {
		MagicCard card = new MagicCard();
		card.setName(name);
		card.setSet(set);
		return card;
	}

	public static IPriceProvider create(Type type) {
		return providers[type.ordinal()];
	}
}
