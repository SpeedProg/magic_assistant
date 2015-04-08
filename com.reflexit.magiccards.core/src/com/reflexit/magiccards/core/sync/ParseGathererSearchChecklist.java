package com.reflexit.magiccards.core.sync;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

public class ParseGathererSearchChecklist extends AbstractParseGathererSearch {
	/*-
	 <tr class="cardItem">
	    <td class="number">159</td>
	    <td class="name"><a class="nameLink" href="../Card/Details.aspx?multiverseid=265718" onclick="return CardLinkAction(event, this, 'SameWindow');">Acidic Slime</a></td>
	    <td class="artist">Karl Kopinski</td>
	    <td class="color">Green</td>
	    <td class="rarity">U</td>
	    <td class="set">Magic 2013</td>
	 </tr>
	 */
	private static final Pattern cardItemPattern = Pattern.compile("<tr class=\"cardItem");
	static Pattern idPattern = Pattern.compile("href=.*/Card/Details.aspx\\?multiverseid=(\\d+)");
	static Pattern namePattern = Pattern.compile("name\">.*>(.*)</a></td>");
	static Pattern artistPattern = Pattern.compile("artist\">(.*)</td>");
	static Pattern setPattern = Pattern.compile("set\">(.*)</td>");
	static Pattern numberPattern = Pattern.compile("number\">(.*)</td>");

	@Override
	public boolean processFromReader(BufferedReader st, GatherHelper.ILoadCardHander handler)
			throws IOException {
		String line = "";
		boolean cards = false;
		boolean lastpage = false;
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
				parseRecord(line, handler);
				cards = true;
				continue;
			} else if (lastPagePattern.matcher(line).find()) {
				lastpage = true;
			}
		}
		if (cards == false)
			throw new RuntimeException("No results");
		return lastpage;
	}

	private void parseRecord(String line, GatherHelper.ILoadCardHander handler) {
		String[] trs = line.split("<tr");
		for (int i = 0; i < trs.length; i++) {
			String tdline = trs[i];
			if (!tdline.contains("<td"))
				continue;
			MagicCard card = new MagicCard();
			// split by td
			String[] rows = tdline.split("<td");
			if (rows.length == 0)
				continue;
			String id = GatherHelper.getMatch(idPattern, rows[2]);
			card.setId(id);
			String num = GatherHelper.getMatch(numberPattern, rows[1]);
			card.setCollNumber(num);
			String name = GatherHelper.getMatch(namePattern, rows[2]);
			card.setName(name);
			String artist = GatherHelper.getMatch(artistPattern, rows[3]);
			card.setArtist(artist);
			String set = GatherHelper.getMatch(setPattern, rows[6]);
			card.setSet(set);
			// print
			handler.handleCard(card);
		}
	}

	@Override
	public boolean loadSet(String set, GatherHelper.ILoadCardHander handler, ICoreProgressMonitor monitor)
			throws IOException {
		loadMultiPageUrl(GatherHelper.getSearchQuery("checklist", set, true), handler, set, monitor);
		return true;
	}

	public static void main(String[] args) throws MalformedURLException, IOException {
		GatherHelper.OutputHandler handler = new GatherHelper.OutputHandler(System.out, true, true);
		new ParseGathererSearchChecklist().loadSingleUrl(
				GatherHelper.getSearchQuery("checklist&sort=cn+", "Magic 2013", false), handler);
		System.err.println("Total " + handler.getCardCount());
	}
}
