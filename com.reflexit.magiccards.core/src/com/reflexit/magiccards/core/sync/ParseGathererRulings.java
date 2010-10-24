/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *    Terry Long - refactored ParseGathererLegality to instead retrieve rulings on cards
 *
 *******************************************************************************/
package com.reflexit.magiccards.core.sync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.ICardModifiable;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IStorage;
import com.reflexit.magiccards.core.model.storage.IStorageContainer;

/**
 * Retrieve legality info
 */
public class ParseGathererRulings {
	private static final String GATHERER_URL_BASE = "http://gatherer.wizards.com/";
	private static final String RULINGS_QUERY_URL_BASE = GATHERER_URL_BASE + "Pages/Card/Details.aspx?multiverseid=";
	private static Charset UTF_8 = Charset.forName("utf-8");
	/*-
	 <div id="ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_rulingsContainer" class="postContainer" style="display:block;">
	                        <table cellpadding="0" cellspacing="0">

	                                    <tr class="post evenItem" style="background-color: #efefef;">
	                                        <td id="ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_rulingsRepeater_ctl00_rulingDate" style="width: 70px; padding-left: 10px; font-weight: bold;">2/1/2007</td>

	                                        <td id="ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_rulingsRepeater_ctl00_rulingText" style="width: 610px; padding-right: 5px;">Removes all creature abilities. This includes mana abilities. Animated lands will also lose the ability to tap for mana. </td>

	                                    </tr>

	                        </table>
	                    </div>

	 */
	private static Pattern rulingPattern = Pattern.compile("<td.*?rulingText.*?5px;\">(.+?)</td>");
	private static Pattern ratingPattern = Pattern.compile("class=\"textRatingValue\">([0-9.]{1,5})</span");
	private static Pattern artistPattern = Pattern.compile("ArtistCredit\"\\sclass=\"value\">.*?\">(.*?)</a>");
	/*-
	      <div id="ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_numberRow" class="row">
	                        <div class="label">
	                            Card #:</div>
	                        <div class="value">
	                            33</div>
	                    </div>

	 */
	private static Pattern cardnumPattern = Pattern.compile("Card #:</div>\\s*<div class=\"value\">\\s*(.*?)</div>");
	/*-
	      <div class="cardtextbox">When Anathemancer enters the battlefield, it deals damage to target player equal to the number of nonbasic lands that player controls.</div>
	      <div class="cardtextbox">Unearth <img src="/Handlers/Image.ashx?size=small&amp;name=5&amp;type=symbol" alt="5" align="absbottom" /><img src="/Handlers/Image.ashx?size=small&amp;name=B&amp;type=symbol" alt="Black" align="absbottom" /><img src="/Handlers/Image.ashx?size=small&amp;name=R&amp;type=symbol" alt="Red" align="absbottom" /> <i>(<img src="/Handlers/Image.ashx?size=small&amp;name=5&amp;type=symbol" alt="5" align="absbottom" /><img src="/Handlers/Image.ashx?size=small&amp;name=B&amp;type=symbol" alt="Black" align="absbottom" /><img src="/Handlers/Image.ashx?size=small&amp;name=R&amp;type=symbol" alt="Red" align="absbottom" />: Return this card from your graveyard to the battlefield. It gains haste. Exile it at the beginning of the next end step or if it would leave the battlefield. Unearth only as a sorcery.)</i></div></div>

	 */
	private static Pattern oraclePattern = Pattern.compile("<div class=\"cardtextbox\">(.*?)</div>");
	/*-
	 * <div class="label"> 
	 Other Sets:</div> 
	 <div class="value"> 
	 <div id="ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_otherSetsValue"> 
	 <a href="Details.aspx?multiverseid=19789"><img title="Mercadian Masques (Uncommon)" src="../../Handlers/Image.ashx?type=symbol&amp;set=MM&amp;size=small&amp;rarity=U" alt="Mercadian Masques (Uncommon)" align="absmiddle" style="border-width:0px;" /></a>
	 <a href="Details.aspx?multiverseid=48432"><img title="Mirrodin (Common)" src="../../Handlers/Image.ashx?type=symbol&amp;set=MRD&amp;size=small&amp;rarity=C" alt="Mirrodin (Common)" align="absmiddle" style="border-width:0px;" /></a> 
	 </div> 
	 </div> 
	 */
	private static Pattern otherSetPattern = Pattern.compile("Other Sets:</div>\\s*<div class=\"value\">\\s*(.*?)</div>");
	private static Pattern otherSetPatternEach = Pattern.compile("multiverseid=(\\d+)\"><img title=\"(.*?) \\((.*?)\\)");

	private void parseSingleCard(IMagicCard card, Set<ICardField> fieldMap) throws IOException {
		URL url = new URL(RULINGS_QUERY_URL_BASE + card.getCardId());
		InputStream openStream = url.openStream();
		BufferedReader st = new BufferedReader(new InputStreamReader(openStream, UTF_8));
		String line;
		String html = "";
		while ((line = st.readLine()) != null) {
			html += line + " ";
		}
		st.close();
		extractField(card, fieldMap, html, MagicCardField.RULINGS, rulingPattern);
		extractField(card, fieldMap, html, MagicCardField.RATING, ratingPattern);
		extractField(card, fieldMap, html, MagicCardField.ARTIST, artistPattern);
		extractField(card, fieldMap, html, MagicCardField.COLLNUM, cardnumPattern);
		extractField(card, fieldMap, html, MagicCardField.ORACLE, oraclePattern);
		extractOtherSets(card, fieldMap, html);
	}

	protected void extractOtherSets(IMagicCard card, Set<ICardField> fieldMap, String html) {
		if (fieldMap == null || fieldMap.contains(MagicCardField.SET)) {
			Matcher matcher1 = otherSetPattern.matcher(html);
			String setsHtml = "";
			if (matcher1.find()) {
				setsHtml = matcher1.group(1).trim();
			} else
				return;
			ICardStore db = DataManager.getCardHandler().getMagicDBStore();
			Matcher matcher = otherSetPatternEach.matcher(setsHtml);
			while (matcher.find()) {
				String id = matcher.group(1).trim();
				if (id.length() == 0)
					continue;
				String set = matcher.group(2).trim();
				String rarity = matcher.group(3).trim();
				// other printings
				MagicCard mcard;
				if (card instanceof MagicCard) {
					mcard = (MagicCard) card;
				} else if (card instanceof MagicCardPhisical) {
					mcard = ((MagicCardPhisical) card).getCard();
				} else {
					continue;
				}
				MagicCard card2 = (MagicCard) mcard.clone();
				card2.setId(id);
				card2.setSet(set.trim());
				card2.setRarity(rarity.trim());
				if (db.getCard(card2.getCardId()) == null) {
					db.add(card2);
					System.err.println("Added " + card2.getName() + " " + id + " " + set + " " + rarity);
				}
			}
		}
	}

	protected void extractField(IMagicCard card, Set<ICardField> fieldMap, String html, MagicCardField field, Pattern pattern) {
		if (fieldMap == null || fieldMap.contains(field)) {
			Matcher matcher = pattern.matcher(html);
			String value = "";
			while (matcher.find()) {
				String v = matcher.group(1).trim();
				if (v.length() == 0)
					continue;
				if (value.length() > 0)
					value += "\n";
				value += v;
			}
			if (value.length() != 0) {
				if (field == MagicCardField.ORACLE) {
					value = value.replaceAll("\\n", "<br>");
					value = ParseGathererNewVisualSpoiler.htmlToString(value);
				}
				((ICardModifiable) card).setObjectByField(field, value);
			}
		}
	}

	public void updateCard(IMagicCard magicCard, IProgressMonitor monitor, Set<ICardField> fieldMap) throws IOException {
		monitor.beginTask("Loading additional info...", 10);
		monitor.worked(1);
		try {
			// load individual card
			try {
				parseSingleCard(magicCard, fieldMap);
			} catch (IOException e) {
				System.err.println("Cannot load card " + e.getMessage() + " " + magicCard.getCardId());
			}
			monitor.worked(8);
		} finally {
			monitor.done();
		}
	}

	public void updateStore(Iterator<IMagicCard> iter, int size, IProgressMonitor monitor, Set<ICardField> fieldMaps) throws IOException {
		monitor.beginTask("Loading additional info...", size + 10);
		ICardStore db = DataManager.getCardHandler().getMagicDBFilteredStore().getCardStore();
		IStorage storage = ((IStorageContainer) db).getStorage();
		storage.setAutoCommit(false);
		monitor.worked(5);
		try {
			for (int i = 0; iter.hasNext(); i++) {
				IMagicCard magicCard = iter.next();
				if (monitor.isCanceled())
					return;
				// load individual card
				monitor.subTask("Updating card " + i + " of " + size);
				try {
					parseSingleCard(magicCard, fieldMaps);
				} catch (IOException e) {
					System.err.println("Cannot load card " + e.getMessage() + " " + magicCard.getCardId());
				}
				if (magicCard instanceof MagicCardPhisical) {
					db.update(((MagicCardPhisical) magicCard).getCard());
				} else {
					db.update(magicCard);
				}
				if (fieldMaps.contains(MagicCardField.ID)) {
					// load and cache image
					CardCache.createCardURL(magicCard, true, true);
				}
				monitor.worked(1);
			}
		} finally {
			storage.setAutoCommit(true);
			storage.save();
			monitor.worked(5);
			monitor.done();
		}
	}

	public static void main(String[] args) throws IOException {
		MagicCard card = new MagicCard();
		card.setCardId(19789);
		// card.setCardId(11179);
		ParseGathererRulings parser = new ParseGathererRulings();
		parser.parseSingleCard(card, null);
		System.err.println(card.getRulings() + " " + card.getArtist() + " " + card.getCommunityRating() + " " + card.getCollNumber());
	}
}
