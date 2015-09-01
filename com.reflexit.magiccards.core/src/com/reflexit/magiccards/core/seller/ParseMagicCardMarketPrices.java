package com.reflexit.magiccards.core.seller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.sync.WebUtils;

public class ParseMagicCardMarketPrices extends AbstractPriceProvider {
	private final String setURL = "https://www.magiccardmarket.eu/?mainPage=advancedSearch";
	private final String baseURL = "https://www.magiccardmarket.eu/?mainPage=advancedSearch&idExpansion=SET&resultsPage=PAGE";
	private final String singleCardURL = "https://www.magiccardmarket.eu/?mainPage=advancedSearch&idExpansion=SET&cardName=NAME";
	private static final Pattern setsPattern = Pattern
			.compile("<select name=\\\"idExpansion\\\"[^>]*>(.*?)</select>");
	private static final Pattern setItemPattern = Pattern
			.compile("<option[ ]*value=\\\"(\\d*?)\\\">(.*?)</option>");
	private static final Pattern paginationPattern = Pattern
			.compile("(\\d*)?\" onmouseover=\"showMsgBox\\('Last page'\\)\"");
	/* Mapping between MCM and MA set name.
	 * Key: MCN set name
	 * Value: MA set name
	 */
	private static final Map<String, String> setNameMapping = new HashMap<String, String>();
	private static final List<String> harperPrismPromosSingleCards = new ArrayList<String>();
	private static final List<String> setName1ToNMappingDDAnthology = new ArrayList<String>();
	static {
		/* Known name mappings (relation 1:1) */
		setNameMapping.put("Alpha", "Limited Edition Alpha");
		setNameMapping.put("Battle Royale", "Battle Royale Box Set");
		setNameMapping.put("Beatdown", "Beatdown Box Set");
		setNameMapping.put("Beta", "Limited Edition Beta");
		setNameMapping.put("Commander", "Magic: The Gathering-Commander");
		setNameMapping.put("Commander 2013", "Commander 2013 Edition");
		setNameMapping.put("Conspiracy", "Magic: The Gathering\u2014Conspiracy");
		setNameMapping
				.put("Duel Decks: Phyrexia vs. The Coalition", "Duel Decks: Phyrexia vs. the Coalition");
		setNameMapping.put("From the Vault: Annihilation", "From the Vault: Annihilation (2014)");
		setNameMapping.put("Magic 2015", "Magic 2015 Core Set");
		setNameMapping.put("Magic 2014", "Magic 2014 Core Set");
		setNameMapping.put("Revised", "Revised Edition");
		setNameMapping.put("Sixth Edition", "Classic Sixth Edition");
		setNameMapping.put("Ugin's Fate Promos", "Ugin's Fate promos");
		setNameMapping.put("Unlimited", "Unlimited Edition");
		setNameMapping.put("Planechase 2012", "Planechase 2012 Edition");
		setNameMapping
				.put("Premium Deck Series: Fire & Lightning", "Premium Deck Series: Fire and Lightning");
		setNameMapping.put("Prerelease Promos", "Promo set for Gatherer"); // split also into online set Harper Prism Promos
		// Seems to be unknown to MKM because they are from Magic Online Game only
		//setNameMapping.put("", "Masters Edition");
		//setNameMapping.put("", "Masters Edition III");
		//setNameMapping.put("", "Masters Edition II");
		//setNameMapping.put("", "Masters Edition IV");
		//setNameMapping.put("", "Vintage Masters");
		//setNameMapping.put("", "Tempest Remastered");
		// MApping between one online set name and multi offline names (relation 1:n)
		setName1ToNMappingDDAnthology.add("Duel Decks Anthology, Divine vs. Demonic");
		setName1ToNMappingDDAnthology.add("Duel Decks Anthology, Elves vs. Dragons");
		setName1ToNMappingDDAnthology.add("Duel Decks Anthology, Elves vs. Goblins");
		setName1ToNMappingDDAnthology.add("Duel Decks Anthology, Garruk vs. Liliana");
		setName1ToNMappingDDAnthology.add("Duel Decks Anthology, Jace vs. Chandra");
		// Single cards that are in "Promo set for Gatherer" (offline) and must be put into
		// "Harper Prism Promos" (online)
		harperPrismPromosSingleCards.add("Arena");
		harperPrismPromosSingleCards.add("Giant Badger");
		harperPrismPromosSingleCards.add("Mana Crypt");
		harperPrismPromosSingleCards.add("Sewers of Estark");
		harperPrismPromosSingleCards.add("Windseeker Centaur");
	}
	private static final String onlineSetNameDDAnthology = "Duel Decks: Anthology";
	private static ParseMagicCardMarketPrices instance = new ParseMagicCardMarketPrices();

	public static ParseMagicCardMarketPrices getInstance() {
		return instance;
	}

	private ParseMagicCardMarketPrices() {
		super("MagicCardMarket");
	}

	@Override
	public Iterable<IMagicCard> updatePrices(final Iterable<IMagicCard> iterable,
			final ICoreProgressMonitor monitor) throws IOException {
		final Map<String, List<IMagicCard>> offlineSets = new HashMap<String, List<IMagicCard>>();
		for (IMagicCard magicCard : iterable) {
			if (offlineSets.containsKey(magicCard.getSet())) {
				offlineSets.get(magicCard.getSet()).add(magicCard);
			} else {
				offlineSets.put(magicCard.getSet(), new ArrayList<IMagicCard>());
				offlineSets.get(magicCard.getSet()).add(magicCard);
			}
		}
		// check relation 1:n and set the offline set name to the correct online set name
		for (String set : setName1ToNMappingDDAnthology) {
			if (offlineSets.containsKey(set)) {
				List<IMagicCard> l = offlineSets.remove(set);
				List<IMagicCard> addL = offlineSets.get(onlineSetNameDDAnthology);
				if (addL == null) {
					offlineSets.put(onlineSetNameDDAnthology, l);
				} else {
					addL.addAll(l);
				}
			}
		}
		// Single promo card mapping
		List<IMagicCard> promos = offlineSets.get("Promo set for Gatherer");
		if (promos != null) {
			for (IMagicCard card : promos) {
				for (String cardname : harperPrismPromosSingleCards) {
					if (cardname.equals(card.getEnglishName())) {
						List<IMagicCard> addL = offlineSets.get("Harper Prism Promos");
						if (addL == null) {
							List<IMagicCard> l = new ArrayList<IMagicCard>();
							l.add(card);
							offlineSets.put("Harper Prism Promos", l);
						} else {
							addL.add(card);
						}
					}
				}
			}
			// remove "Harper Prism Promos" card from "Promo set for Gatherer" list
			List<IMagicCard> l = offlineSets.get("Harper Prism Promos");
			if (l != null && !l.isEmpty()) {
				for (IMagicCard card : l) {
					promos.remove(card);
				}
			}
		}
		// Set splits offline into two parts online it is just one
		if (offlineSets.containsKey("Time Spiral \"Timeshifted\"")) {
			List<IMagicCard> l = offlineSets.remove("Time Spiral \"Timeshifted\"");
			List<IMagicCard> addL = offlineSets.get("Time Spiral");
			if (addL == null) {
				offlineSets.put("Time Spiral", l);
			} else {
				addL.addAll(l);
			}
		}
		monitor.beginTask("Loading " + getName() + " prices...", offlineSets.size() + 10);
		Map<String, String> onlineSets = getOnlineSets();
		Iterator<String> offlineSetNames = offlineSets.keySet().iterator();
		monitor.worked(10);
		// Create all needed actions
		List<Action> actions = new ArrayList<Action>();
		while (offlineSetNames.hasNext()) {
			actions.add(new Action(monitor, offlineSetNames.next(), onlineSets, offlineSets));
		}
		MagicLogger.debug("Getting online set prices for provider [" + getName() + "] started.");
		// Execute all actions and wait for some until they are finished.
		@SuppressWarnings("unchecked")
		Map<Action, Map<String, Float>> result = new CallableExecutor(monitor, 10, actions).exec();
		// Test output (if uncomment this, comment the CallableExecutor above)
		Iterator<Entry<Action, Map<String, Float>>> it = result.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Action, Map<String, Float>> entry = it.next();
			//System.err.println("Result: Set ["+entry.getKey().toString()+"], Size ["+entry.getValue().size()+"]");
		}
		MagicLogger.debug("Getting online set prices for provider [" + getName() + "] finished.");
		return iterable;
	}

	/**
	 * Get all available set found online.
	 *
	 * @return A map where the key is
	 * @throws IOException
	 */
	public Map<String, String> getOnlineSets() throws IOException {
		try {
			return processSetFile(new URL(setURL));
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	/**
	 * Search all possible sets online in the response of specified URL and
	 * return a list with set names as key and the depending online id as value.<br/>
	 * Online id is used to get the prices based on URL {@link #baseURL} where set SET
	 * placeholder will be changed with.
	 *
	 * @param url
	 *            URL where sets could be found
	 * @return A map where its keys are magic assistant set names and keys
	 *         its associated online MCM set id.
	 * @throws IOException
	 */
	private Map<String, String> processSetFile(URL url) throws IOException {
		MagicLogger.debug("Getting online sets for provider [" + getName() + "] ...");
		Map<String, String> result = new HashMap<String, String>();
		try (
				BufferedReader br = new BufferedReader(new InputStreamReader(WebUtils.openUrl(url)))) {
			String line;
			while ((line = br.readLine()) != null) {
				Matcher allSetsMatcher = setsPattern.matcher(line);
				if (allSetsMatcher.find()) {
					line = allSetsMatcher.group(0);
					Matcher m = setItemPattern.matcher(line);
					while (m.find()) {
						String id = m.group(1);
						if (id != null && id.length() > 0) {
							String name = m.group(2).trim();
							if (setNameMapping.containsKey(name)) {
								name = setNameMapping.get(name);
							}
							result.put(name, id);
						}
					}
					break;
				}
			}
		}
		MagicLogger.debug("Getting online Sets for provider [" + getName() + "] finished. Result: " + result);
		return result;
	}

	@Override
	public URL getURL() {
		try {
			return new URL("https://www.magiccardmarket.eu");
		} catch (MalformedURLException e) {
			return null;
		}
	}

	@Override
	public Currency getCurrency() {
		String cur = getProperties().getProperty("currency");
		if (cur == null) {
			return Currency.getInstance("EUR");
		}
		return Currency.getInstance(cur);
	}

	/**
	 * {@link Callable} implementation that can be executed concurrently
	 * with an {@link CallableExecutor executor}.
	 *
	 * @see CallableExecutor
	 */
	private class Action implements Callable<Map<String, Float>> {
		private final Map<String, Float> prices = new HashMap<String, Float>();
		private final String offlineSetName;
		private final Map<String, String> onlineSets;
		private final Map<String, List<IMagicCard>> offlineSets;
		private final ICoreProgressMonitor monitor;

		public Action(ICoreProgressMonitor monitor, String offlineSetName, Map<String, String> onlineSets,
				Map<String, List<IMagicCard>> offlineSets) {
			this.offlineSetName = offlineSetName;
			this.onlineSets = onlineSets;
			this.offlineSets = offlineSets;
			this.monitor = monitor;
		}

		@Override
		public Map<String, Float> call() {
			final String mappingSetID = onlineSets.get(offlineSetName);
			// Yep, I know, it is concurrent but it is better than nothing.
			monitor.subTask("Loading set " + offlineSetName);
			if (mappingSetID != null) {
				try {
					// Try to make single card fetch as faster as a bulk fetch for a single card
					// Value 10 is a guessed pagination average. If size is smaller a single card
					// fetch will be done.
					if (offlineSets.get(offlineSetName).size() <= 10) {
						List<IMagicCard> offlineCards = offlineSets.get(offlineSetName);
						offlineCards.size();
						String cardName;
						for (IMagicCard magicCard : offlineCards) {
							parseSingleCard(mappingSetID, magicCard);
							cardName = changeNameHTMLToDB(magicCard.getName(), magicCard.getFlipId() != 0);
							if (prices.containsKey(cardName)) {
								Float price = prices.get(cardName);
								setDbPrice(magicCard, price, getCurrency());
							} else if (prices.containsKey(changeNameHTMLToDB(magicCard.getEnglishName(),
									magicCard.getFlipId() != 0))) {
								Float price = prices.get(changeNameHTMLToDB(magicCard.getEnglishName(),
										magicCard.getFlipId() != 0));
								setDbPrice(magicCard, price, getCurrency());
							}
						}
					} else {
						// bulk fetch for all online cards in a set
						parse(mappingSetID);
						if (!prices.isEmpty()) {
							List<IMagicCard> offlineCards = offlineSets.get(offlineSetName);
							for (IMagicCard magicCard : offlineCards) {
								String cardName = changeNameHTMLToDB(magicCard.getName(),
										magicCard.getFlipId() != 0);
								if (prices.containsKey(cardName)) {
									Float price = prices.get(cardName);
									setDbPrice(magicCard, price, getCurrency());
								} else if (prices.containsKey(changeNameHTMLToDB(magicCard.getEnglishName(),
										magicCard.getFlipId() != 0))) {
									Float price = prices.get(changeNameHTMLToDB(magicCard.getEnglishName(),
											magicCard.getFlipId() != 0));
									setDbPrice(magicCard, price, getCurrency());
								}
							}
						}
					}
				} catch (Exception e) {
					MagicLogger.log(e);
				}
			}
			monitor.worked(1);
			return prices;
		}

		@Override
		public String toString() {
			return offlineSetName;
		}

		/**
		 * Change card name from HTML MCM name to MA DB name.
		 * 
		 * @param cardName
		 *            Card name
		 * @param isFlipCard
		 *            Is this card a Flip Card. If so, there is some more to change.
		 * @return Return card name in MA format
		 */
		private String changeNameHTMLToDB(String cardName, boolean isFlipCard) {
			String result = cardName.replaceAll("\\+", " ");
			// I thought that magicCard.getFlipId() != 0 do work, but is only available on a small set. so check for // also
			if (isFlipCard || result.contains("//")) {
				if (result.indexOf('(') > 0) {
					result = result.substring(0, result.lastIndexOf('(')).trim();
				}
			}
			return result;
		}

		/**
		 * Change card name from MA DB name to HTML MCM name.
		 * 
		 * @param cardName
		 * @param isFlipCard
		 * @return
		 */
		private String changeNameDBToHTML(String cardName, boolean isFlipCard) {
			String result = cardName.replaceAll(" ", "+");
			if (isFlipCard || result.contains("//")) {
				result = result.replaceAll("//", "%2F%2F");
				if (result.indexOf('(') > 0) {
					result = result.substring(0, result.lastIndexOf('(')).trim();
				}
			}
			return result;
		}

		/**
		 * Files online card prices for specified set id. This is the recursive start
		 * for {@link #getOnlineSetCardPrices(String, int, int)} (root call).
		 *
		 * @param setId
		 *            MagicCardMArket set id.
		 * @throws IOException
		 */
		private void parse(String setId) throws IOException {
			String url = baseURL.toString().replace("SET", setId);
			getOnlineSetCardPrices(url, 0, -1);
		}

		/**
		 * Parse a single card from specified set
		 * 
		 * @param setId
		 *            set id (online number)
		 * @param card
		 *            Card name
		 * @throws IOException
		 */
		private void parseSingleCard(String setId, IMagicCard card) throws IOException {
			String cardName = changeNameDBToHTML(card.getEnglishName(), card.getFlipId() != 0);
			String url = singleCardURL.toString().replace("SET", setId).replace("NAME", cardName);
			URL url2 = new URL(url);
			String line = WebUtils.openUrlText(url2);
			// If full content of the URL response is null it waits some time and do
			// another request. This can run endless therefore it can be canceled with
			// monitor. I have no clue why this appends.
			while (line == null || line.length() <= 0) {
				if (monitor.isCanceled()) {
					return;
				}
				// Test output
				//System.err.println("Failed loading page for provider ["+getName()+"]: " + url);
				MagicLogger.trace("Failed loading page for provider [" + getName() + "]: " + url);
				//
				// milliseconds to wait.
				int nextRetry = 10 * 1000;
				long startTime = System.currentTimeMillis();
				long currentTime = 0;
				do {
					// Do something.
					currentTime = System.currentTimeMillis();
				} while (startTime + (nextRetry) > currentTime);
				// Next request
				line = WebUtils.openUrlText(url2);
			}
			getPrices(line);
		}

		/**
		 * Fills resulting {@link #prices} with data from URL.
		 * Root call should start with pagination 0 and maxPagination -1 to know
		 * that pagination must be found on the page.
		 *
		 * @param url
		 *            {@link baseURL} where SET is already replaced with an id.
		 * @param pagination
		 *            Root call should start with pagination 0
		 * @param maxPagination
		 *            Root call should start with maxPagination -1
		 * @throws IOException
		 * @see {@link #parse(String)}
		 */
		private void getOnlineSetCardPrices(String url, int pagination, int maxPagination) throws IOException {
			if (monitor.isCanceled()) {
				return;
			}
			// Test output
			//System.err.println("Loading page for provider ["+getName()+"]: " + url.replace("PAGE", "" + pagination));
			MagicLogger.trace("Loading page for provider [" + getName() + "]: "
					+ url.replace("PAGE", "" + pagination));
			int newMaxPagination = -1;
			URL url2 = new URL(url.replace("PAGE", "" + pagination));
			// I know I get the full side here, but HTTPS returns sometimes an
			// empty response with and response code 200.
			// This is easy to check and can be made better
			String line = WebUtils.openUrlText(url2);
			// If full content of the URL response is null it waits some time and do
			// another request. This can run endless therefore it can be canceled with
			// monitor. I have no clue why this appends.
			while (line == null || line.length() <= 0) {
				if (monitor.isCanceled()) {
					return;
				}
				// Test output
				//System.err.println("Failed loading page for provider ["+getName()+"]: "+url.replace("PAGE", "" + pagination));
				MagicLogger.trace("Failed loading page for provider [" + getName() + "]: "
						+ url.replace("PAGE", "" + pagination));
				//
				// milliseconds to wait.
				int nextRetry = 10 * 1000;
				long startTime = System.currentTimeMillis();
				long currentTime = 0;
				do {
					// Do something.
					currentTime = System.currentTimeMillis();
				} while (startTime + (nextRetry) > currentTime);
				// Next request
				line = WebUtils.openUrlText(url2);
			}
			if (pagination == 0 && maxPagination == -1) {
				// Sure now, that it is in root call of this recursive function
				// (on the first pagination page).
				// Get maximum pagination from first side, and fetch prices in one step
				if (newMaxPagination < 0) {
					Matcher paginationMatcher = paginationPattern.matcher(line);
					if (paginationMatcher.find()) {
						newMaxPagination = Integer.parseInt(paginationMatcher.group(1));
					}
				}
				// Fetch online card names and prices.
				getPrices(line);
				if (newMaxPagination < 0) {
					// Make sure that max pagination is not -1 anymore so that upper if statement
					// will not be executed in the next recursive step.
					newMaxPagination = 0;
				}
				if (pagination <= newMaxPagination) {
					// last pagination not reached.
					getOnlineSetCardPrices(url, pagination + 1, newMaxPagination);
				}
			} else {
				// Fetch online card names and prices.
				newMaxPagination = maxPagination;
				getPrices(line);
				if (pagination <= newMaxPagination) {
					// last pagination not reached.
					newMaxPagination = maxPagination;
					getOnlineSetCardPrices(url, pagination + 1, newMaxPagination);
				}
			}
		}

		/**
		 * Parse for prices.
		 *
		 * @param line
		 */
		private void getPrices(String line) {
			Pattern priceRowPAttern = Pattern.compile("<tr class=\"row_(Odd|Even){1} row_\\d*\">(.*?)</tr>");
			Matcher priceRowMatcher = priceRowPAttern.matcher(line);
			while (priceRowMatcher.find()) {
				Pattern pricePattern = Pattern.compile("<a[^>]*>(.*?)</a>.*>(.*?) &#x20AC;");
				Matcher priceMatcher = pricePattern.matcher(priceRowMatcher.group(2));
				while (priceMatcher.find()) {
					String sPrice = priceMatcher.group(2);
					sPrice = sPrice.replace("&#x20AC;", ""); // Euro sign character
					sPrice = sPrice.trim();
					NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);
					try {
						Number number = format.parse(sPrice);
						String name = priceMatcher.group(1);
						name = name.replaceAll("\\(Version \\d\\)", "").trim();
						prices.put(name, number.floatValue());
					} catch (ParseException e) {
						//						prices.put(priceMatcher.group(1), new Float(0));
					}
					// Test output
					// System.err.println("Online card price found. Name [" + priceMatcher.group(1) + "], price [" + sPrice + "]");
					MagicLogger.trace("Online card price found. Name [" + priceMatcher.group(1)
							+ "], price [" + sPrice + "]");
				}
			}
		}
	}

	public static void main(String[] args) throws IOException {
		ParseMagicCardMarketPrices item = new ParseMagicCardMarketPrices();
		// Testing to get online sets
		System.err.println("Sets: " + item.getOnlineSets());
	}
}
