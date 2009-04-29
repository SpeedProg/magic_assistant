package com.reflexit.magiccards.core.sync;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;

public class ParseGathererNewVisualSpoiler {
	private static Charset UTF_8 = Charset.forName("utf-8");
	/*-
	  <tr class="cardItem evenItem">
	                <td class="leftCol">
	                    <div class="clear"></div>
	                    <a href="../Card/Details.aspx?multiverseid=154408" id="ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_ctl00_listRepeater_ctl00_cardImageLink" onclick="return CardLinkAction(event, this, 'SameWindow');">
	                        <img src="../../Handlers/Image.ashx?multiverseid=154408&amp;type=card" id="ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_ctl00_listRepeater_ctl00_cardImage" width="95" height="132" alt="Advice from the Fae" border="0" />
	                    </a>
	                    <div class="clear"></div>
	                </td>
	                <td class="middleCol">
	                    <div class="clear"></div>
	                    <div class="cardInfo">
	                        <span class="cardTitle">
	                            <a id="ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_ctl00_listRepeater_ctl00_cardTitle" onclick="return CardLinkAction(event, this, 'SameWindow');" href="../Card/Details.aspx?multiverseid=154408">Advice from the Fae</a></span> <span class="manaCost">
	                                <img src="/Handlers/Image.ashx?size=small&amp;name=2U&amp;type=symbol" alt="Two or Blue" align="absbottom" /><img src="/Handlers/Image.ashx?size=small&amp;name=2U&amp;type=symbol" alt="Two or Blue" align="absbottom" /><img src="/Handlers/Image.ashx?size=small&amp;name=2U&amp;type=symbol" alt="Two or Blue" align="absbottom" /></span> (<span class="convertedManaCost">6</span>)
	                        <br />
	                        <span class="typeLine">
	                            Sorcery
	                            </span>
	                        <br />
	                        <div class="rulesText">
	                            <p><i>(<img src="/Handlers/Image.ashx?size=small&amp;name=2U&amp;type=symbol" alt="Two or Blue" align="absbottom" /> can be paid with any two mana or with <img src="/Handlers/Image.ashx?size=small&amp;name=U&amp;type=symbol" alt="Blue" align="absbottom" />. This card's converted mana cost is 6.)</i></p><p>Look at the top five cards of your library. If you control more creatures than any other player, put two of those cards into your hand. Otherwise, put one of them into your hand. Then put the rest on the bottom of your library in any order.</p></div>
	                    </div>
	                </td>
	                <td class="rightCol setVersions">
	                    <div class="clear"></div>
	                    <div>
	                        <div id="ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_ctl00_listRepeater_ctl00_cardSetCurrent" class="rightCol">
	<a onclick="return CardLinkAction(event, this, 'SameWindow');" href="../Card/Details.aspx?multiverseid=154408"><img title="Shadowmoor (Uncommon)" src="../../Handlers/Image.ashx?type=symbol&amp;set=SHM&amp;size=small&amp;rarity=U" alt="Shadowmoor (Uncommon)" style="border-width:0px;" /></a>
	</div>
	                    </div>
	                    
	                </td>
	            </tr>

	 * 
	 * */
	public static interface ILoadCardHander {
		void handle(MagicCard card);

		void edition(String edition, String edAddr);
	}
	public static class OutputHandler implements ILoadCardHander {
		private PrintStream out;

		public OutputHandler(PrintStream st) {
			this.out = st;
		}

		public void handle(MagicCard card) {
			TextPrinter.print(card, this.out);
		}

		public void edition(String edition, String edAddr) {
			Editions.getInstance().addAbbrLocale(edition, edAddr, null);
		}
	}
	private static String base = "http://gatherer.wizards.com/Pages/Search/Default.aspx?output=standard";
	private static String[] updateAll = { //
	base + "&format=[%22Legacy%22]", //
	        base + "&set=[%22Unhinged%22]", //
	        base + "&set=[%22Unglued%22]", //
	};
	private static String[] updateLatest = { base + "&format=[%22Standard%22]" };
	private static IProgressMonitor monitor;

	public static void main(String[] args) throws IOException {
		String from = args[0];
		String to = null;
		if (args.length > 1) {
			to = args[1];
		}
		if (from.equals("updateAll")) {
			updateAll(to, updateAll);
		} else
			parseFileOrUrl(from, to, new NullProgressMonitor());
		Editions.getInstance().save();
	}

	private static void updateAll(String to, String[] urls) throws MalformedURLException, IOException {
		PrintStream out = System.out;
		if (to != null)
			out = new PrintStream(new File(to));
		TextPrinter.printHeader(IMagicCard.DEFAULT, out);
		for (String string : urls) {
			System.err.println("Loading " + string);
			loadUrl(new URL(string), new OutputHandler(out));
		}
		out.close();
	}

	public static boolean loadUrl(URL url, ILoadCardHander handler) throws IOException {
		InputStream openStream = url.openStream();
		BufferedReader st = new BufferedReader(new InputStreamReader(openStream, UTF_8));
		boolean res = processFile(st, handler);
		st.close();
		return res;
	}

	public static void loadFile(File file, ILoadCardHander handler) throws IOException {
		BufferedReader st = new BufferedReader(new InputStreamReader(new FileInputStream(file), UTF_8));
		processFile(st, handler);
		st.close();
	}

	public static void parseFileOrUrl(String from, String to, IProgressMonitor pm) throws FileNotFoundException,
	        MalformedURLException, IOException {
		monitor = pm;
		monitor.beginTask("Downloading", 100);
		PrintStream out = System.out;
		if (to != null)
			out = new PrintStream(new FileOutputStream(new File(to)), true, UTF_8.toString());
		TextPrinter.printHeader(IMagicCard.DEFAULT, out);
		OutputHandler handler = new OutputHandler(out);
		try {
			countCards = 0;
			if (from.startsWith("http:")) {
				// http://ww2.wizards.com/gatherer/index.aspx?setfilter=All%20sets&colorfilter=White&output=Spoiler
				int i = 0;
				boolean lastPage = false;
				while (lastPage == false && i < 1000 && monitor.isCanceled() == false) {
					URL url = new URL(from + "&page=" + i);
					lastPage = loadUrl(url, handler);
					i++;
					if (countCards == 0)
						monitor.worked(1);
					else
						monitor.worked(i * 100 / (countCards / 25));
				}
			} else {
				File input = new File(from);
				loadFile(input, handler);
			}
		} finally {
			out.close();
			monitor.done();
		}
	}
	private static Pattern countPattern = Pattern
	        .compile("Search:<span id=\"ctl00_ctl00_ctl00_MainContent_SubContent_SubContentHeader_searchTermDisplay\"><i>.*</i>  \\((\\d+)\\)</span>");
	private static Pattern lastPagePattern = Pattern
	        .compile("\\Q<span style=\"visibility:hidden;\">&nbsp;&gt;</span></div>");
	private static int countCards;

	private static boolean processFile(BufferedReader st, ILoadCardHander handler) throws IOException {
		String line = "";
		int state = 0;
		boolean lastPage = false;
		boolean cards = false;
		while ((state == 0 && (line = st.readLine()) != null) || (state == 1)) {
			Matcher cm = countPattern.matcher(line);
			if (cm.find()) {
				countCards = Integer.parseInt(cm.group(1));
			}
			if (lastPagePattern.matcher(line).find()) {
				lastPage = true;
			}
			if (line.matches(".*class=\"cardItem .*")) {
				String tr = "";
				do {
					if (line.matches(".*</tr>.*")) {
						state = 1;
						break;
					}
					tr += line + " ";
				} while ((line = st.readLine()) != null);
				parseRecord(tr, handler);
				cards = true;
				continue;
			}
			state = 0;
		}
		if (cards == false)
			throw new IOException("No results");
		return lastPage;
	}
	static Pattern spanPattern = Pattern.compile("class=[^>]*>(.*)</span>");
	static Pattern divPattern = Pattern.compile("class=[^>]*>(.*?)</div>");
	static Pattern idPattern = Pattern.compile("href=.*/Card/Details.aspx\\?multiverseid=(\\d+)");
	static Pattern setPattern = Pattern.compile("title=\"(.*) \\((.*)\\)\" src=");
	static Pattern namePattern = Pattern.compile(".*>(.*)</a></span>");
	static Pattern powPattern = Pattern.compile("\\((\\d+/)?(\\d+)\\)");

	private static void parseRecord(String line, ILoadCardHander handler) {
		MagicCard card = new MagicCard();
		// split by td
		String[] rows = line.split("<td");
		String[] fields = rows[2].split("<span|<div");
		card.setId(getMatch(idPattern, fields[3]));
		card.setName(getMatch(namePattern, fields[3]));
		String cost = getMatch(spanPattern, fields[4]);
		card.setCost(cost);
		String type = getMatch(spanPattern, fields[6]);
		String powerCombo = type;
		String pow = getMatch(powPattern, powerCombo, 1).replaceFirst("/", "");
		String tou = getMatch(powPattern, powerCombo, 2);
		type = type.replaceAll("\\(.*", "").trim();
		card.setType(type);
		String text = fixText(getMatch(divPattern, fields[7]));
		card.setOracleText(text);
		card.setPower(pow);
		card.setToughness(tou);
		String[] sets = rows[3].split("<img");
		for (String set : sets) {
			String edition = getMatch(setPattern, set, 1);
			String rarity = getMatch(setPattern, set, 2);
			if (edition.length() <= 1)
				continue;
			card.setSet(edition.trim());
			card.setRarity(rarity.trim());
			handler.edition(edition, null);
			break; // TODO this would remember only first set
		}
		// print
		handler.handle(card);
	}

	private static String getMatch(Pattern textPattern, String typeF) {
		return getMatch(textPattern, typeF, 1);
	}

	private static String getMatch(Pattern textPattern, String line, int g) {
		Matcher matcher;
		matcher = textPattern.matcher(line);
		String text = "";
		if (matcher.find()) {
			text = matcher.group(g);
			if (text == null)
				text = "";
		}
		String res = htmlToString(text).trim();
		if (res.length() == 0)
			res = " ";
		return res;
	}
	static Map manaMap = new LinkedHashMap();
	static {
		manaMap.put("\\Q{500}", "{0.5}");
		manaMap.put("\\{(\\d)(\\w)\\}", "{$1/$2}");
		manaMap.put("\\{(\\w)(\\w)\\}", "{$1/$2}");
		manaMap.put("\\Q{tap}", "{T}");
		manaMap.put("\\Q{untap}", "{Q}");
	}

	private static String fixText(String str1) {
		String str = str1;
		str = str.replaceAll("</p><p>", "<br>");
		str = str.replaceFirst("<p>", "");
		str = str.replaceFirst("</p>", "");
		return str;
	}

	private static String htmlToString(String str) {
		str = str.replaceAll("&nbsp;", " ");
		str = str.replaceAll("&amp;", "&");
		str = str.replaceAll("&apos;", "'");
		if (str.contains("img")) {
			str = str.replaceAll("<img [^<]*name=([^&]*)&[^>]*/>", "{$1}");
			for (Iterator iterator = manaMap.keySet().iterator(); iterator.hasNext();) {
				String alt = (String) iterator.next();
				String to = (String) manaMap.get(alt);
				str = str.replaceAll(alt, to);
			}
		}
		return str;
	}

	public static URL createImageURL(int cardId, String editionAbbr, String locale) throws MalformedURLException {
		return new URL("http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=" + cardId + "&type=card");
	}

	public static void downloadUpdates(String set, String file, IProgressMonitor pm) throws FileNotFoundException,
	        MalformedURLException, IOException {
		String url;
		if (set == null || set.equals("Standard")) {
			url = updateLatest[0];
		} else {
			set = set.replaceAll(" ", "%20");
			url = base + "&set=[%22" + set + "%22]";
		}
		parseFileOrUrl(url, file, pm);
	}
}
