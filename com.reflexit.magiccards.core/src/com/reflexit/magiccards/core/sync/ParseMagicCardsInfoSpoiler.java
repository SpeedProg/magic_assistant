package com.reflexit.magiccards.core.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public class ParseMagicCardsInfoSpoiler extends ParserHtmlHelper {
	/*-
	<td valign="top" width="25%">
	    <span style="font-size: 1.2em;"><a href="/at/en/4.html">Volcanic Dragon</a></span>
	    <p><img src="http://magiccards.info/images/en.gif" alt="English" class="flag2" height="11" width="16"> Anthologies, <i>Special</i></p>

	      <p>Creature — Dragon 4/4,
	        4RR (6)</p>
	      <p class="ctext"><b>Flying<br><br>Haste (This creature can attack and {T} as soon as it comes under your control.)</b></p>

	    <p><i>Speed and fire are always a deadly combination.</i></p>
	    <p>Illus. Janine Johnston</p>
	</td>
	 */
	private static String baseSearch = "http://magiccards.info/query?q=";
	private static final Pattern cardItemPattern = Pattern.compile("<td valign");
	private static final Pattern cardItemPatternEnd = Pattern.compile("</td>");
	static final Pattern countPattern = Pattern.compile("(\\d+) cards\\s*");
	static Pattern setRarityPattern = Pattern.compile("> (.*), <i>(.*)</i>");
	static Pattern nameNumPattern = Pattern.compile("(\\d+)\\.html\">(.*)</a>");
	static Pattern typeCostPattern = Pattern.compile(">(.*),(?:(.*) \\()?");
	static Pattern typeCreaturePattern = Pattern.compile("(.*) ([^ ]*)/([^ ]*)");
	static Pattern textPattern = Pattern.compile("class=\"ctext\"><b>(.*)</b>");
	static Pattern artistPattern = Pattern.compile("Illus. (.*)</p>");

	public boolean processFromReader(BufferedReader st, ILoadCardHander handler) throws IOException {
		String line = "";
		boolean cards = false;
		String cardtext = "";
		boolean in = false;
		while ((line = st.readLine()) != null) {
			if (countPattern.matcher(line).find()) {
				Matcher matcher = countPattern.matcher(line);
				try {
					matcher.find();
					int c = Integer.parseInt(matcher.group(1));
					handler.setCardCount(c);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			} else if (cardItemPattern.matcher(line).find()) {
				cardtext = "";
				in = true;
				continue;
			} else if (in && cardItemPatternEnd.matcher(line).find()) {
				parseRecord(cardtext, handler);
				cards = true;
				in = false;
			} else if (in) {
				cardtext += line;
			}
		}
		if (cards == false)
			throw new RuntimeException("No results");
		return true;
	}

	private void parseRecord(String line, ILoadCardHander handler) {
		if (!line.contains("<span"))
			return;
		MagicCard card = new MagicCard();
		// split by td
		String[] rows = line.split("<p");
		if (rows.length == 0)
			return;
		String num = getMatch(nameNumPattern, rows[0], 1);
		card.setCollNumber(num);
		String name = getMatch(nameNumPattern, rows[0], 2);
		card.setName(name);
		String type = getMatch(typeCostPattern, rows[2], 1);
		if (type.indexOf('-') > 0) {
			type = type.replaceAll("-", " -");
		}
		Matcher typeMatcher = typeCreaturePattern.matcher(type);
		if (typeMatcher.matches()) {
			card.setType(typeMatcher.group(1));
			card.setPower(typeMatcher.group(2));
			card.setToughness(typeMatcher.group(3));
		} else {
			card.setType(type);
		}
		String cost = getMatch(typeCostPattern, rows[2], 2);
		String normalizedCost = normalizeCost(cost);
		card.setCost(normalizedCost);
		String set = getMatch(setRarityPattern, rows[1], 1);
		if (set != null && set.length() > 0) {
			String abbr = Editions.getInstance().getAbbrByName(set).toLowerCase(Locale.ENGLISH);
			card.setSet(set);
			card.set(MagicCardField.IMAGE_URL, "http://magiccards.info/scans/en/" + abbr + "/" + num
					+ ".jpg");
		}
		String rar = getMatch(setRarityPattern, rows[1], 2);
		card.setRarity(rar);
		String artist = getMatch(artistPattern, rows[5]);
		card.setArtist(artist);
		card.setOracleText(getMatch(textPattern, rows[3]));
		card.setCardId(card.syntesizeId());
		// print
		handler.handleCard(card);
	}

	private String normalizeCost(String cost) {
		int l = cost.length();
		if (cost.trim().length() == 0)
			return "";
		StringBuilder res = new StringBuilder(l * 3);
		for (int i = 0; i < l; i++) {
			char c = cost.charAt(i);
			res.append('{');
			res.append(c);
			res.append('}');
		}
		return res.toString();
	}

	public boolean loadSet(String set, ILoadCardHander handler, ICoreProgressMonitor monitor)
			throws IOException {
		try {
			monitor.beginTask("Downloading " + set + " checklist", 100);
			return loadSingleUrl(getSearchQuery(set), handler);
		} finally {
			monitor.done();
		}
	}

	public boolean loadSingleUrl(URL url, ILoadCardHander handler) throws IOException {
		try {
			String html = WebUtils.openUrlText(url);
			boolean res = processFromReader(FileUtils.openBufferedReader(html), handler);
			return res;
		} catch (IOException e) {
			MagicLogger.log("Loading url exception: " + url + ": " + e.getMessage());
			throw e;
		}
	}

	public void loadFile(File file, ILoadCardHander handler) throws IOException {
		BufferedReader st = FileUtils.openBuferedReader(file);
		processFromReader(st, handler);
		st.close();
	}

	public static URL getSearchQuery(String set) throws MalformedURLException {
		String url;
		if (set != null && set.startsWith("http")) {
			url = set;
		} else {
			String out = "&v=spoiler&s=issue";
			String abbr = Editions.getInstance().getAbbrByName(set);
			String base = baseSearch + "e%3A" + abbr + "%2Fen" + out;
			url = base;
		}
		return new URL(url);
	}

	public static void main(String[] args) throws MalformedURLException, IOException {
		OutputHandler handler = new OutputHandler(System.out, true, true);
		System.out
				.println("ID|NAME|COST|TYPE|POWER|TOUGHNESS|ORACLE|SET|RARITY|DBPRICE|LANG|RATING|ARTIST|COLLNUM|RULINGS|TEXT|ENID|PROPERTIES");
		Editions.getInstance().addEdition("Duels of the Planeswalkers", "dpa");
		new ParseMagicCardsInfoSpoiler().loadSingleUrl(getSearchQuery("Duels of the Planeswalkers"), handler);
		System.err.println("Total " + handler.getCardCount());
	}
}
