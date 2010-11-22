package com.reflexit.magiccards.core.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

public class ParseGathererNewTextSpoiler {
	/*-
	<tr>
	                <td>
	                    Name:
	                </td>
	                <td>

	                    <a id="ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_ctl00_cardEntries_ctl00_cardLink" class="nameLink" onclick="return CardLinkAction(event, this, 'SameWindow');" href="../Card/Details.aspx?multiverseid=154408">Advice from the Fae</a>
	                </td>
	            </tr>
	            <tr>
	                <td>
	                    Cost:
	                </td>
	                <td>
	                    (2/U)(2/U)(2/U)
	                </td>

	            </tr>
	            <tr>
	                <td>
	                    Type:
	                </td>
	                <td>
	                    Sorcery
	                </td>
	            </tr>
	            <tr>

	                <td>
	                    Pow/Tgh:
	                </td>
	                <td>
	                    
	                </td>
	            </tr>
	            <tr>
	                <td>
	                    Rules Text:
	                </td>

	                <td>
	                    ({(}2/u)} can be paid with any two mana or with {U}. This card's converted mana cost is 6.)
	Look at the top five cards of your library. If you control more creatures than any other player, put two of those cards into your hand. Otherwise, put one of them into your hand. Then put the rest on the bottom of your library in any order.
	                </td>
	            </tr>
	            <tr>
	                <td>
	                    Set/Rarity:
	                </td>
	                <td>
	                    Shadowmoor Uncommon
	                </td>

	            </tr>
	            <tr>
	                <td colspan="2">
	                    <br />
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
			Editions.getInstance().addAbbr(edition, edAddr);
		}
	}

	private static String base = "http://gatherer.wizards.com/Pages/Search/Default.aspx?output=spoiler&method=text";
	private static String[] updateAll = { //
	base + "&typefilter=Lands", //
			base + "&typefilter=Artifacts",//
			base + "&colorfilter=White", //
			base + "&colorfilter=Black",//
			base + "&colorfilter=Blue",//
			base + "&colorfilter=Green",//
			base + "&colorfilter=Red", //
			base + "&colorfilter=Multi-Color", //
	};
	private static String[] updateLatest = { base + "&format=[%22Standard%22]" };

	public static void main(String[] args) throws IOException {
		String from = args[0];
		String to = null;
		if (args.length > 1) {
			to = args[1];
		}
		if (from.equals("updateAll")) {
			updateAll(to, updateAll);
		} else
			parseFileOrUrl(from, to);
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

	public static void loadUrl(URL url, ILoadCardHander handler) throws IOException {
		InputStream openStream = url.openStream();
		BufferedReader st = new BufferedReader(new InputStreamReader(openStream));
		processFile(st, handler);
		st.close();
	}

	public static void loadFile(File file, ILoadCardHander handler) throws IOException {
		Charset encoding = Charset.forName("utf-8");
		BufferedReader st = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
		processFile(st, handler);
		st.close();
	}

	public static void parseFileOrUrl(String from, String to) throws FileNotFoundException, MalformedURLException, IOException {
		PrintStream out = System.out;
		if (to != null)
			out = new PrintStream(new File(to));
		TextPrinter.printHeader(IMagicCard.DEFAULT, out);
		OutputHandler handler = new OutputHandler(out);
		if (from.startsWith("http:")) {
			// http://ww2.wizards.com/gatherer/index.aspx?setfilter=All%20sets&colorfilter=White&output=Spoiler
			URL url = new URL(from);
			loadUrl(url, handler);
		} else {
			File input = new File(from);
			loadFile(input, handler);
		}
		out.close();
	}

	private static void processFile(BufferedReader st, ILoadCardHander handler) throws IOException {
		String line = "";
		int state = 0;
		while ((state == 0 && (line = st.readLine()) != null) || (state == 1)) {
			if (line.matches(".*class=\"nameLink\".*")) {
				String tr = "";
				do {
					if (line.matches(".*td colspan=\"2\".*")) {
						state = 1;
						break;
					}
					tr += line + " ";
				} while ((line = st.readLine()) != null);
				parseRecord(tr, handler);
				continue;
			}
			state = 0;
		}
		;
	}

	static Pattern textPattern = Pattern.compile("</td>\\s*<td>([^<]*)</td>");
	static Pattern idPattern = Pattern.compile("href=.*/Card/Details.aspx\\?multiverseid=(\\d+)");
	static Pattern setPattern = Pattern.compile("(.*) (Mythic Rare|\\w+)\\s*$");
	static Pattern namePattern = Pattern.compile(">(.*)</a>");
	static Pattern powPattern = Pattern.compile("\\((\\d+/)?(\\d+)\\)");

	private static void parseRecord(String line, ILoadCardHander handler) {
		MagicCard card = new MagicCard();
		// split by td
		String[] strings = line.split("<tr>");
		card.setId(getMatch(idPattern, strings[0]));
		card.setName(getMatch(namePattern, strings[0]));
		String cost = manaCoding(getMatch(textPattern, strings[1]));
		card.setCost(cost);
		card.setType(getMatch(textPattern, strings[2]));
		String text = fixText(getMatch(textPattern, strings[4]));
		card.setOracleText(text);
		String powerCombo = getMatch(textPattern, strings[3]);
		String pow = getMatch(powPattern, powerCombo, 1).replaceFirst("/", "");
		String tou = getMatch(powPattern, powerCombo, 2);
		card.setPower(pow);
		card.setToughness(tou);
		String[] sets = getMatch(textPattern, strings[5]).split(",");
		for (String set : sets) {
			String edition = getMatch(setPattern, set, 1);
			String rarity = getMatch(setPattern, set, 2);
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
		manaMap.put("\\(", "{");
		manaMap.put("\\)", "}");
		manaMap.put("^(\\d)", "{$1}");
		manaMap.put("^([UWBGRXYZ])", "{$1}");
		manaMap.put("\\}(\\d)", "}{$1}");
		manaMap.put("\\}([UWBGRXYZ])", "}{$1}");
		manaMap.put("Snow Mana", "{S}");
		manaMap.put("Half Mana", "{0.5}");
		manaMap.put("Tap", "{T}");
		manaMap.put("Untap", "{Q}");
	}
	private static Pattern textBug1 = Pattern.compile("\\{\\(\\}([uwbrg0-9])/([uwbrg0-9])\\)\\}");

	private static String fixText(String str1) {
		String str = str1;
		// bug in current version {(}b/r)}
		Matcher matcher = textBug1.matcher(str1);
		while (matcher.find()) {
			String l1 = matcher.group(1);
			String l2 = matcher.group(2);
			String res = ("{" + l1 + "/" + l2 + "}").toUpperCase();
			str = str.replaceAll("\\{\\(\\}" + l1 + "/" + l2 + "\\)\\}", res);
		}
		return str;
	}

	private static String manaCoding(String str1) {
		String str = str1;
		for (Iterator iterator = manaMap.keySet().iterator(); iterator.hasNext();) {
			String alt = (String) iterator.next();
			String to = (String) manaMap.get(alt);
			String strN = str.replaceAll(alt, to);
			while (strN != str) {
				str = strN;
				strN = str.replaceAll(alt, to);
			}
		}
		str = str.replaceAll("\\}\\}", "}");
		str = str.replaceAll("\\{\\{", "{");
		str = str.replaceAll("\\}/\\{", "/");
		return str;
	}

	private static String htmlToString(String str) {
		str = str.replaceAll("&nbsp;", " ");
		str = str.replaceAll("&amp;", "&");
		str = str.replaceAll("&apos;", "'");
		str = str.replaceAll("<font[^>]*><b>", "");
		str = str.replaceAll("</b></font>", "");
		str = str.replaceAll("<font[^>]*>", "");
		str = str.replaceAll("</font>", "");
		return str;
	}

	public static URL createImageURL(int cardId, String editionAbbr, String locale) throws MalformedURLException {
		return new URL("http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=" + cardId + "&type=card");
	}
}
