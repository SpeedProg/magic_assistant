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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.ICardHandler;
import com.reflexit.magiccards.core.model.ICardModifiable;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.storage.ICardStore;

/**
 * Retrieve legality info
 */
public class ParseGathererRulings extends ParseGathererPage {
	private static final String RULINGS_QUERY_URL_BASE = GATHERER_URL_BASE + "Pages/Card/Details.aspx?multiverseid=";
	private IMagicCard card;
	private Set<ICardField> fieldMapFilter;
	private ICardStore magicDb;
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

	void parseSingleCard(IMagicCard card, Set<ICardField> fieldMap, IProgressMonitor monitor) throws IOException {
		setCard(card);
		setFilter(fieldMap);
		load(monitor);
	}

	protected void extractOtherSets(IMagicCard card, Set<ICardField> fieldMap, String html) {
		if (fieldMap == null || fieldMap.contains(MagicCardField.SET)) {
			Matcher matcher1 = otherSetPattern.matcher(html);
			String setsHtml = "";
			if (matcher1.find()) {
				setsHtml = matcher1.group(1).trim();
			} else
				return;
			Matcher matcher = otherSetPatternEach.matcher(setsHtml);
			while (matcher.find()) {
				String id = matcher.group(1).trim();
				if (id.length() == 0)
					continue;
				String set = matcher.group(2).trim();
				String rarity = matcher.group(3).trim();
				// other printings
				MagicCard mcard = card.getBase();
				MagicCard card2 = (MagicCard) mcard.cloneCard();
				card2.setId(id);
				card2.setSet(set.trim());
				card2.setRarity(rarity.trim());
				if (magicDb != null && magicDb.getCard(card2.getCardId()) == null) {
					magicDb.add(card2);
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

	public static Set<ICardField> getAllExtraFields() {
		HashSet<ICardField> res = new HashSet<ICardField>();
		res.add(MagicCardField.RATING);
		res.add(MagicCardField.ARTIST);
		res.add(MagicCardField.COLLNUM);
		res.add(MagicCardField.ORACLE);
		return res;
	}

	public void updateCard(IMagicCard magicCard, Set<ICardField> fieldMap, IProgressMonitor monitor) throws IOException {
		try {
			ICardHandler cardHandler = DataManager.getCardHandler();
			if (cardHandler != null)
				setMagicDb(cardHandler.getMagicDBStore());
			parseSingleCard(magicCard, fieldMap, new SubProgressMonitor(monitor, 8));
		} catch (IOException e) {
			Activator.log("Cannot load card " + e.getMessage() + " " + magicCard.getCardId());
		}
	}

	public void setCard(IMagicCard card) {
		this.card = card;
	}

	public void setFilter(Set<ICardField> fieldMapFilter) {
		this.fieldMapFilter = fieldMapFilter;
	}

	@Override
	protected void loadHtml(String html, IProgressMonitor monitor) {
		monitor.beginTask("Updating card", 6);
		try {
			html = html.replaceAll("\r?\n", " ");
			extractField(card, fieldMapFilter, html, MagicCardField.RULINGS, rulingPattern);
			monitor.worked(1);
			extractField(card, fieldMapFilter, html, MagicCardField.RATING, ratingPattern);
			monitor.worked(1);
			extractField(card, fieldMapFilter, html, MagicCardField.ARTIST, artistPattern);
			monitor.worked(1);
			extractField(card, fieldMapFilter, html, MagicCardField.COLLNUM, cardnumPattern);
			monitor.worked(1);
			extractField(card, fieldMapFilter, html, MagicCardField.ORACLE, oraclePattern);
			monitor.worked(1);
			extractOtherSets(card, fieldMapFilter, html);
			monitor.worked(1);
		} finally {
			monitor.done();
		}
	}

	@Override
	protected String getUrl() {
		return RULINGS_QUERY_URL_BASE + card.getCardId();
	}

	public void setMagicDb(ICardStore magicDb) {
		this.magicDb = magicDb;
	}

	public static void main(String[] args) throws IOException {
		MagicCard card = new MagicCard();
		card.setCardId(191338);
		// card.setCardId(191338);
		ParseGathererRulings parser = new ParseGathererRulings();
		parser.setCard(card);
		parser.load(new NullProgressMonitor());
		System.err.println(card.getRulings() + " " + card.getArtist() + " " + card.getCommunityRating() + " " + card.getCollNumber());
	}
}
