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
import com.reflexit.magiccards.core.model.Edition;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public class ParseMagicCardsInfoChecklist extends ParserHtmlHelper {
	/*-
	<tbody><tr>
	<th align="right" width="30"><b>№</b></th>
	<th><b>Card name</b></th>
	<th><b>Type</b></th>
	<th><b>Mana</b></th>
	<th><b>Rarity</b></th>
	<th><b>Artist</b></th>
	<th><b>Edition</b></th>
	</tr>
	
	
	<tr class="even">
	<td align="right">1</td>
	<td><a href="/at/en/1.html">Nevinyrral's Disk</a></td>
	<td>Artifact</td>
	<td>4</td>
	<td>Special</td>
	<td>Mark Tedin</td>
	<td><img src="http://magiccards.info/images/en.gif" alt="English" class="flag2" height="11" width="16"> Anthologies</td>
	</tr>
	
	
	<tr class="odd">
	<td align="right">2</td>
	<td><a href="/at/en/2.html">Goblin King</a></td>
	<td>Summon Lord 2/2</td>
	<td>1RR</td>
	<td>Special</td>
	<td>Jesper Myrfors</td>
	<td><img src="http://magiccards.info/images/en.gif" alt="English" class="flag2" height="11" width="16"> Anthologies</td>
	</tr>
	 */
	private static String baseSearch = "http://magiccards.info/query?q=";
	private static final Pattern cardItemPattern = Pattern.compile("<tr class=");
	private static final Pattern cardItemPatternEnd = Pattern.compile("</tr>");
	static final Pattern countPattern = Pattern.compile("(\\d+) cards\\s*");
	static Pattern plainPattern = Pattern.compile(".*>(.*)</td>");
	static Pattern namePattern = Pattern.compile("<a href=\"/([a-z0-9]+)/([a-z]+)/[0-9a-z]+.html\">(.*)</a></td>");
	static Pattern typeCreaturePattern = Pattern.compile("(.*) ([^ ]*)/([^ ]*)");
	private URL url;

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
		if (!line.contains("<td"))
			return;
		MagicCard card = new MagicCard();
		// split by td
		String[] rows = line.split("<td");
		if (rows.length == 0)
			return;
		String num = getMatch(plainPattern, rows[1]);
		card.setCollNumber(num);
		String name = getMatch(namePattern, rows[2], 3);
		card.setName(name);
		String abbr = getMatch(namePattern, rows[2], 1);
		String lang = getMatch(namePattern, rows[2], 2);
		String type = getMatch(plainPattern, rows[3]);
		Matcher typeMatcher = typeCreaturePattern.matcher(type);
		if (typeMatcher.matches()) {
			card.setType(typeMatcher.group(1));
			card.setPower(typeMatcher.group(2));
			card.setToughness(typeMatcher.group(3));
		} else {
			card.setType(type);
		}
		String cost = getMatch(plainPattern, rows[4]);
		card.setCost(normalizeCost(cost));
		String rar = getMatch(plainPattern, rows[5]);
		card.setRarity(rar);
		String artist = getMatch(plainPattern, rows[6]);
		card.setArtist(artist);
		String set = getMatch(plainPattern, rows[7]);
		Edition ed = new Edition(set, abbr.toUpperCase(Locale.ENGLISH));
		handler.handleEdition(ed);
		card.setSet(set);
		String regset = Editions.getInstance().getNameByAbbr(ed.getMainAbbreviation());
		if (regset != null) {
			card.setSet(regset);
		}
		card.set(MagicCardField.IMAGE_URL, "http://magiccards.info/scans/" + lang + "/" + abbr + "/" + num + ".jpg");
		card.setCardId(card.syntesizeId());
		card.setLanguage(lang);
		// print
		handler.handleCard(card);
	}

	private String normalizeCost(String cost) {
		int l = cost.length();
		if (cost.trim().length() == 0)
			return "";
		boolean inMana = false;
		StringBuilder res = new StringBuilder(l * 3);
		for (int i = 0; i < l; i++) {
			char c = cost.charAt(i);
			if (c == '{') {
				inMana = true;
				res.append(c);
				continue;
			}
			if (inMana) {
				res.append(c);
				if (c == '}')
					inMana = false;
				continue;
			}
			res.append('{');
			res.append(c);
			res.append('}');
		}
		return res.toString();
	}

	public boolean loadSet(String set, ILoadCardHander handler, ICoreProgressMonitor monitor) throws IOException {
		try {
			monitor.beginTask("Downloading " + set + " checklist", 100);
			return loadSingleUrl(getSearchQuery(set), handler);
		} finally {
			monitor.done();
		}
	}

	public boolean loadSingleUrl(URL url, ILoadCardHander handler) throws IOException {
		try {
			this.url = url;
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
			String out = "&v=list&s=issue";
			String abbr = Editions.getInstance().getAbbrByName(set);
			if (abbr == null)
				abbr = set;
			String base = baseSearch + "e%3A" + abbr + "%2Fen" + out;
			url = base;
		}
		return new URL(url);
	}

	public static void main(String[] args) throws MalformedURLException, IOException {
		OutputHandler handler = new OutputHandler(System.out, true, true);
		// Editions.getInstance().addEdition("Duels of the Planeswalkers",
		// "dpa");
		// http://magiccards.info/query?q=Spite&v=list&s=cname
		// URL searchQuery = getSearchQuery("dpa");
		// new ParseMagicCardsInfoChecklist().loadSingleUrl(searchQuery,
		// handler);
		String german = "http://magiccards.info/query?q=e%3Adtk%2Fde&v=list&s=cname";
		String doubles = "http://magiccards.info/query?q=Spite&v=list&s=cname";
		String wc = "http://magiccards.info/query?q=e%3Awmcq%2Fen&v=list&s=cname";
		String surl = german;
		new ParseMagicCardsInfoChecklist().loadSingleUrl(new URL(surl),
				handler);
		System.err.println("Total " + handler.getCardCount());
	}
}
