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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;

public class ParseGathererSpoiler {
	/*-
	 <tr onmouseover="this.style.backgroundColor='#F5DEB3';" onmouseout="this.style.backgroundColor='#FBF2E1';" style="color:Black;background-color:#FBF2E1;">
	 <td align="left" valign="top" onclick="javascript:openDetailsWindow(132073);" onmouseover="this.style.cursor='pointer';" onmouseout="this.style.cursor='';" style="font-weight:bold;width:250px;"
	 >Fog Elemental</td>
	 <td align="left" valign="top" onclick="javascript:openDetailsWindow(132073);" onmouseover="this.style.cursor='pointer';" onmouseout="this.style.cursor='';" style="white-space:nowrap;"
	 ><img src='http://resources.wizards.com/magic/images/symbols/Symbol_2_mana.gif' alt='2 Mana' border='0'/>
	 <img src='http://resources.wizards.com/magic/images/symbols/blue_mana.gif' width='12' height='12' alt='Blue Mana' border='0'/>
	 </td>
	 <td align="left" valign="top" onclick="javascript:openDetailsWindow(132073);" onmouseover="this.style.cursor='pointer';" onmouseout="this.style.cursor='';" style="white-space:nowrap;"
	 >Creature - Elemental</td>
	 <td align="left" valign="top" onclick="javascript:openDetailsWindow(132073);" onmouseover="this.style.cursor='pointer';" onmouseout="this.style.cursor='';" style="width:300px;"
	 >Flying <i>(This creature can't be blocked except by creatures with flying or reach.)</i>
	 <br>When Fog Elemental attacks or blocks, sacrifice it at end of combat.</td>
	 <td align="left" valign="top" onclick="javascript:openDetailsWindow(132073);" onmouseover="this.style.cursor='pointer';" onmouseout="this.style.cursor='';"
	 >4</td>
	 <td align="left" valign="top" onclick="javascript:openDetailsWindow(132073);" onmouseover="this.style.cursor='pointer';" onmouseout="this.style.cursor='';" style="width:25px;"
	 >4</td>
	 <td align="left" valign="top" style="width:150px;"><a href="javascript:void(0);" onclick="javascript:window.open('CardDetails.aspx?id=132073', '_magicCardDetails', 'width=750, height=500, scrollbars=1, resizable=1');" title='Tenth Edition (Uncommon)'>
	 <img src='http://resources.wizards.com/Magic/Images/Expsym/exp_sym_10E_U.gif' border='0' alt='Tenth Edition (Uncommon)'/></a>

	 <a href="javascript:void(0);" onclick="javascript:window.open('CardDetails.aspx?id=26657', '_magicCardDetails', 'width=750, height=500, scrollbars=1, resizable=1');" title='Beatdown Box Set (Common)'><img src='http://resources.wizards.com/Magic/Images/Expsym/exp_sym_BD_C.gif' border='0' alt='Beatdown Box Set (Common)'/></a>

	 <a href="javascript:void(0);" onclick="javascript:window.open('CardDetails.aspx?id=16433', '_magicCardDetails', 'width=750, height=500, scrollbars=1, resizable=1');" title='Classic Sixth Edition (Common)'><img src='http://resources.wizards.com/Magic/Images/Expsym/exp_sym_6E_C.gif' border='0' alt='Classic Sixth Edition (Common)'/></a>

	 <a href="javascript:void(0);" onclick="javascript:window.open('CardDetails.aspx?id=4486', '_magicCardDetails', 'width=750, height=500, scrollbars=1, resizable=1');" title='Weatherlight (Common)'><img src='http://resources.wizards.com/Magic/Images/Expsym/exp_sym_WL_C.gif' border='0' alt='Weatherlight (Common)'/></a>

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

	private static String base = "http://ww2.wizards.com/gatherer/index.aspx?output=Spoiler&setfilter=All%20sets";
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
	private static String[] updateLatest = { base + "&setfilter=Standard" };

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
			if (line.matches(".*<tr onmouseover=\"this.style.backgroundColor.*")) {
				String tr = "";
				line = line.replaceFirst("^\t\t</tr>", "");
				do {
					if (line.matches(".*</tr>.*")) {
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

	static Pattern textPattern = Pattern.compile("[^>]*>(.*)</td>.*");
	Pattern textPatternCont = Pattern.compile("(.*)</td>.*");
	Pattern textPatternOpen = Pattern.compile("[^>]*>(.*)");
	static Pattern idPattern = Pattern.compile(".*openDetailsWindow\\((\\d+)\\).*");
	static Pattern secIdPattern = Pattern.compile(".*\\Q'CardDetails.aspx?id=\\E(\\d+)'.*");
	static Pattern editionPattern = Pattern.compile(".* alt='(.*)'/>.*");
	static Pattern edAbbrPattern = Pattern.compile(".*/exp_sym_(.*)_.\\.gif.*");

	private static void parseRecord(String line, ILoadCardHander handler) {
		MagicCard card = new MagicCard();
		// split by td
		String[] strings = line.split("<td ");
		card.setId(getMatch(idPattern, strings[1]));
		card.setName(getMatch(textPattern, strings[1]));
		card.setCost(getMatch(textPattern, strings[2]));
		card.setType(getMatch(textPattern, strings[3]));
		card.setOracleText(getMatch(textPattern, strings[4]));
		card.setPower(getMatch(textPattern, strings[5]));
		card.setToughness(getMatch(textPattern, strings[6]));
		String[] sets = strings[7].split("<a href");
		for (int i = 1; i < sets.length; i++) {
			String set = sets[i];
			String id = getMatch(secIdPattern, set);
			if (id.equals(card.getCardId() + "")) {
				String edition = getMatch(editionPattern, set);
				String[] er = edition.split("[\\(\\)]");
				card.setSet(er[0].trim());
				if (er.length > 1)
					card.setRarity(er[1].trim());
				else
					card.setRarity("Unknown");
				// http://resources.wizards.com/Magic/Images/Expsym/exp_sym_TSB_P.gif
				String edAddr = getMatch(edAbbrPattern, set);
				handler.edition(card.getSet(), edAddr);
			}
		}
		// print
		handler.handle(card);
	}

	private static String getMatch(Pattern textPattern, String typeF) {
		Matcher matcher;
		matcher = textPattern.matcher(typeF);
		String type = "";
		if (matcher.matches()) {
			type = matcher.group(1);
		}
		return htmlToString(type);
	}

	static Map manaMap = new HashMap();
	static {
		manaMap.put("Blue Mana", "{U}");
		manaMap.put("White Mana", "{W}");
		manaMap.put("Black Mana", "{B}");
		manaMap.put("Green Mana", "{G}");
		manaMap.put("Red Mana", "{R}");
		manaMap.put("Snow Mana", "{S}");
		manaMap.put("X Mana", "{X}");
		manaMap.put("Y Mana", "{Y}");
		manaMap.put("Z Mana", "{Z}");
		manaMap.put("Half Mana", "{0.5}");
		manaMap.put("R/W Mana", "{R/W}");
		manaMap.put("R/G Mana", "{R/G}");
		manaMap.put("B/R Mana", "{B/R}");
		manaMap.put("B/G Mana", "{B/G}");
		manaMap.put("G/U Mana", "{G/U}");
		manaMap.put("G/W Mana", "{G/W}");
		manaMap.put("W/U Mana", "{W/U}");
		manaMap.put("W/B Mana", "{W/B}");
		manaMap.put("U/B Mana", "{U/B}");
		manaMap.put("U/R Mana", "{U/R}");
		manaMap.put("2/B Mana", "{2/B}");
		manaMap.put("2/R Mana", "{2/R}");
		manaMap.put("2/G Mana", "{2/G}");
		manaMap.put("2/U Mana", "{2/U}");
		manaMap.put("2/W Mana", "{2/W}");
		manaMap.put("Tap", "{T}");
		manaMap.put("Untap", "{Q}");
	}

	private static String htmlToString(String str) {
		str = str.replaceAll("&nbsp;", " ");
		str = str.replaceAll("&amp;", "&");
		str = str.replaceAll("&apos;", "'");
		// str = str.replaceAll("<br>", "\n");
		if (str.contains("img")) {
			str = str.replaceAll("<img [^<]*alt='(\\d+) Mana'[^>]*/>", "{$1}");
			for (Iterator iterator = manaMap.keySet().iterator(); iterator.hasNext();) {
				String alt = (String) iterator.next();
				String to = (String) manaMap.get(alt);
				str = str.replaceAll("<img [^<]*alt='" + alt + "'[^>]*/>", to);
			}
		}
		str = str.replaceAll("<font[^>]*><b>", "");
		str = str.replaceAll("</b></font>", "");
		str = str.replaceAll("<font[^>]*>", "");
		str = str.replaceAll("</font>", "");
		return str;
	}

	public static URL createImageURL(int cardId, String editionAbbr, String locale) throws MalformedURLException {
		return new URL("http://resources.wizards.com/Magic/Cards/" + editionAbbr + "/" + locale + "/Card" + cardId + ".jpg");
	}
}
