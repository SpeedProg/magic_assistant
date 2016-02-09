package com.reflexit.magiccards.core.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.model.Edition;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public class ParseGathererSearchStandard extends AbstractParseGathererSearch {
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
	 */
	@Override
	public boolean loadSet(String set, GatherHelper.ILoadCardHander handler, ICoreProgressMonitor mon)
			throws IOException {
		loadMultiPageUrl(GatherHelper.getSearchQuery("standard", set, true), handler, set, mon);
		return true;
	}

	private void parseFileOrUrl(String from, String to, Properties options, ICoreProgressMonitor pm)
			throws FileNotFoundException,
			MalformedURLException, IOException {
		PrintStream out = System.out;
		if (to != null)
			out = new PrintStream(new FileOutputStream(new File(to)), true, FileUtils.UTF8);
		TextPrinter.printHeader(out);
		GatherHelper.OutputHandler handler = createOutputHandler(out, options);
		try {
			if (from.startsWith("http:")) {
				loadMultiPageUrl(new URL(from), handler, "unknown", pm);
			} else {
				File input = new File(from);
				loadFile(input, handler);
			}
		} finally {
			out.close();
		}
	}

	private static Pattern itemPattern = Pattern.compile("tr class=\"cardItem");
	private static Pattern itemEndPattern = Pattern.compile("</tr>");

	@Override
	protected boolean processFromReader(BufferedReader st, GatherHelper.ILoadCardHander handler)
			throws IOException {
		String line = "";
		int state = 0;
		boolean lastPage = false;
		boolean cards = false;
		while ((state == 0 && (line = st.readLine()) != null) || (state == 1)) {
			if (line == null)
				break;
			int cardCount = findIntegerMatch(countPattern, line, 1, -1);
			if (cardCount != -1) {
				handler.setCardCount(cardCount);
			} else if (lastPagePattern.matcher(line).find()) {
				lastPage = true;
			} else if (itemPattern.matcher(line).find()) {
				String tr = "";
				do {
					if (itemEndPattern.matcher(line).find()) {
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
			throw new RuntimeException("No results");
		return lastPage;
	}

	static Pattern spanPattern = Pattern.compile("class=[^>]*>(.*)</span>");
	static Pattern divPattern = Pattern.compile("class=[^>]*>(.*?)</div>");
	static Pattern idPattern = Pattern.compile("href=.*/Card/Details.aspx\\?multiverseid=(\\d+)");
	static Pattern setPattern = Pattern.compile("title=\"(.*) \\((.*)\\)\" src=.*set=(\\w+)");
	static Pattern namePattern = Pattern.compile(".*>(.*)</a></span>");
	static Pattern powPattern = Pattern.compile("\\(([+*\\d]+/)?([+*\\d]+)\\)");

	private void parseRecord(String line, GatherHelper.ILoadCardHander handler) {
		MagicCard card = new MagicCard();
		// split by td
		String[] rows = line.split("<td");
		if (rows.length < 3)
			return;
		String[] fields = rows[2].split("<span|<div");
		String id = getMatch(idPattern, fields[3]);
		card.setId(id);
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
		fixGathererBugs(card);
		String[] sets = rows[3].split("<a onclick");
		for (String setHtml : sets) {
			String edition = getMatch(setPattern, setHtml, 1);
			String rarity = getMatch(setPattern, setHtml, 2);
			String abbr = getMatch(setPattern, setHtml, 3);
			String setId = getMatch(idPattern, setHtml, 1);
			if (edition.length() <= 1)
				continue;
			edition = edition.trim();
			Edition ed = new Edition(edition, abbr);
			if (id.equals(setId)) {
				card.setSet(edition);
				card.setRarity(rarity.trim());
				handler.handleEdition(ed);
			} else {
				// other printings
				MagicCard card2 = (MagicCard) card.clone();
				card2.setId(setId);
				card2.setSet(edition);
				card2.setRarity(rarity.trim());
				handler.handleEdition(ed);
				handler.handleSecondary(card, card2);
			}
		}
		// print
		handler.handleCard(card);
	}

	private static void fixGathererBugs(MagicCard card) {
		String name = card.getName();
		if (name.contains("’")) {
			int i = name.indexOf('(');
			int k = name.indexOf(')');
			if (i >= 0 && k >= 0) {
				name = name.substring(i + 1, k);
				card.setName(name);
			}
		}
	}

	private static String fixText(String str1) {
		String str = str1;
		str = str.replaceAll("</p><p>", "<br>");
		str = str.replaceFirst("<p>", "");
		str = str.replaceFirst("</p>", "");
		return str;
	}

	void downloadUpdates(String set, String file, Properties options, ICoreProgressMonitor pm)
			throws FileNotFoundException,
			MalformedURLException, IOException {
		String url;
		if (set != null && set.startsWith("http")) {
			url = set;
		} else {
			if (set != null)
				options.put("set", set);
			boolean special = false;
			if (options.get(UpdateCardsFromWeb.UPDATE_SPECIAL) != null)
				special = Boolean.valueOf((String) options.get(UpdateCardsFromWeb.UPDATE_SPECIAL));
			url = GatherHelper.getSearchQuery("standard", set, special).toExternalForm();
		}
		parseFileOrUrl(url, file, options, pm);
	}

	public static void main(String[] args) throws IOException {
		String from = args[0];
		String to = null;
		if (args.length > 1) {
			to = args[1];
		}
		Properties options = new Properties();
		new ParseGathererSearchStandard().parseFileOrUrl(from, to, options, ICoreProgressMonitor.NONE);
		Editions.getInstance().save();
	}
}
