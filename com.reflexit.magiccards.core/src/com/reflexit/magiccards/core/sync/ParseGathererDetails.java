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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.ICardHandler;
import com.reflexit.magiccards.core.model.ICardModifiable;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

/**
 * Retrieve legality info
 */
public class ParseGathererDetails extends ParseGathererPage {
	private static final String DETAILS_QUERY_URL_BASE = GATHERER_URL_BASE + "Pages/Card/Details.aspx?multiverseid=";
	private IMagicCard card;
	private Set<ICardField> fieldMapFilter;
	private ICardStore magicDb;
	/*-
	 * single card rules
	 * 
	 * <table class="cardDetails
	 */
	private static Pattern singleCardPattern = Pattern.compile("<table class=\"cardDetails(.+?)</table>");
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
	private static Pattern textPattern = Pattern.compile("Card Text:</div>(.*?)class=\"label\"");
	private static Pattern textPatternEach = Pattern.compile("<div class=\"cardtextbox\">(.*?)</div>");
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
	/*-
	
	<div id="ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_typeRow" class="row">
	    <div class="label">
	        Types:</div>

	    <div class="value">
	        Creature  — Elemental Warrior</div>
	</div>


	 */
	private static Pattern typesPattern = Pattern.compile("Types:</div>\\s*<div class=\"value\">\\s*(.*?)</div>");
	/*-
	 <div class="contentTitle">

	 <span id="ctl00_ctl00_ctl00_MainContent_SubContent_SubContentHeader_subtitleDisplay">Книга Заклинаний</span>
	 </div>
	 */
	private static Pattern titleNamePattern = Pattern.compile("<div class=\"contentTitle\">\\s*<span id.*?>(.*?)</span>");
	/*-
	 * <div class="label">Card Name:</div>
	   <div class="value">Instigator Gang</div>
	 */
	private static Pattern cardNamePattern = Pattern.compile("Card Name:</div>\\s*<div class=\"value\">\\s*(.*?)</div>");
	/*-
	 *              <img src="../../Handlers/Image.ashx?multiverseid=241988&amp;type=card" 
	 *              id="ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_cardImage" 
	 *              alt="Hinterland Harbor" style="border:none;" />

	 */
	private static Pattern cardAltPattern = Pattern.compile("cardImage\"\\s*alt=\"(.*?)\"");
	/*-
	 *              <img src="../../Handlers/Image.ashx?multiverseid=241988&amp;type=card" 
	 *              id="ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_cardImage" 
	 *              alt="Hinterland Harbor" style="border:none;" />

	 */
	private static Pattern cardIdPattern = Pattern.compile("img src.*?\\?multiverseid=(.*?)&amp;type=card");
	private static Pattern cardRotatePattern = Pattern.compile("img src.*?\\?multiverseid=.*?&amp;type=card&amp;(options=\\w+)");

	void parseSingleCard(IMagicCard card, Set<ICardField> fieldMap, ICoreProgressMonitor monitor) throws IOException {
		setCard(card);
		setFilter(fieldMap);
		load(monitor);
	}

	public void updateCard(IMagicCard magicCard, Set<ICardField> fieldMap, ICoreProgressMonitor monitor) throws IOException {
		ICardHandler cardHandler = DataManager.getCardHandler();
		if (cardHandler != null)
			setMagicDb(cardHandler.getMagicDBStore());
		parseSingleCard(magicCard, fieldMap, monitor);
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
				MagicCard card2 = mcard.cloneCard();
				card2.setId(id);
				card2.setSet(set.trim());
				card2.setRarity(rarity.trim());
				card2.setLanguage(null);
				card2.setText(card.getOracleText());
				if (magicDb != null && magicDb.getCard(card2.getCardId()) == null) {
					magicDb.add(card2);
					System.err.println("Added " + card2.getName() + " " + id + " " + set + " " + rarity);
				}
			}
		}
	}

	protected void extractField(IMagicCard card, Set<ICardField> fieldMap, String html, MagicCardField field, Pattern pattern,
			boolean multiple) {
		if (fieldMap == null || fieldMap.contains(field)) {
			String value = extractPatternValue(html, pattern, multiple);
			if (value.length() != 0) {
				if (field == MagicCardField.TEXT) {
					// hack to correct weird values
					value = value.replaceAll("o([XWGUBR0-9])", "{$1}");
					value = value.replaceAll("ocT", "{T}");
				}
				if (field == MagicCardField.TEXT || field == MagicCardField.ORACLE) {
					value = value.replaceAll("\\n", "<br>");
					value = ParseGathererNewVisualSpoiler.htmlToString(value);
				} else if (field == MagicCardField.TYPE) {
					value = value.replaceAll("—", "-");
					// value = value.replaceAll("—", "-");
					value = value.replaceAll(": -", ":");
					value = value.replaceAll("  *", " ");
				} else if (field == MagicCardField.ARTIST) {
					if (value.length() > 40) {
						value = "none";
					}
				} else if (field == MagicCardField.PART) {
					if (value.startsWith("options=")) {
						value = '@' + value.substring(8);
					}
				}
			} else {
				// empty
				if (field == MagicCardField.PART) {
					value = null;
				}
			}
			((ICardModifiable) card).setObjectByField(field, value);
		}
	}

	public void setCard(IMagicCard card) {
		this.card = card;
	}

	public void setFilter(Set<ICardField> fieldMapFilter) {
		this.fieldMapFilter = fieldMapFilter;
	}

	@Override
	protected void loadHtml(String html0, ICoreProgressMonitor monitor) {
		monitor.beginTask("Updating card", 10);
		try {
			if (card.getCardId() == 0)
				return;
			html0 = html0.replaceAll("\r?\n", " ");
			if (html0.contains("’")) {
				html0 = html0.replace('’', '\'');
			}
			String nameOrig = card.getName(); // original name
			if (nameOrig.indexOf('(') > 0) {
				nameOrig = nameOrig.substring(0, nameOrig.indexOf('(') - 1);
			}
			String nameTitle = extractPatternValue(html0, titleNamePattern, false);
			// name update
			if (nameOrig == null) {
				nameOrig = nameTitle;
				((ICardModifiable) card).setObjectByField(MagicCardField.NAME, nameTitle);
			}
			Matcher matcher0 = singleCardPattern.matcher(html0);
			ArrayList<String> cardSides = new ArrayList<String>(2);
			while (matcher0.find()) {
				String html = matcher0.group(1).trim();
				cardSides.add(html);
			}
			String html = null;
			int sides = cardSides.size();
			if (sides == 1) {
				html = cardSides.get(0);
			} else if (sides == 2) {
				String htmlB = null;
				Iterator<String> iterator = cardSides.iterator();
				String htmlS[] = { iterator.next(), iterator.next() };
				int titleIndex = -1, nameIndex = -1;
				for (int i = 0; i < htmlS.length; i++) {
					String nameCur = extractPatternValue(htmlS[i], cardNamePattern, false);
					if (nameTitle != null && nameTitle.equals(nameCur) && titleIndex == -1) {
						titleIndex = i;
					}
					if (nameOrig != null && nameOrig.equals(nameCur) && nameIndex == -1) {
						nameIndex = i;
						nameTitle = nameCur;
					}
				}
				if (nameIndex >= 0) {
					html = htmlS[nameIndex];
					htmlB = htmlS[1 - nameIndex];
				} else if (titleIndex >= 0) {
					html = htmlS[titleIndex];
					htmlB = htmlS[1 - titleIndex];
				}
				if (html == null) {
					MagicLogger.log("Problems parsing card - cannot find matching name " + nameOrig + " " + card.getCardId());
					return;
				}
				if (htmlB != null) {
					IMagicCard cardB = card.getBase().cloneCard();
					extractField(cardB, null, htmlB, MagicCardField.NAME, cardNamePattern, false);
					extractField(cardB, null, htmlB, MagicCardField.ID, cardIdPattern, false);
					extractField(cardB, null, htmlB, MagicCardField.PART, cardRotatePattern, false);
					int pairId = cardB.getCardId();
					if (pairId != 0) {
						if (magicDb != null && !magicDb.contains(cardB)) {
							extractField(cardB, null, htmlB, MagicCardField.ARTIST, artistPattern, false);
							extractField(cardB, null, htmlB, MagicCardField.COLLNUM, cardnumPattern, false);
							((ICardModifiable) cardB).setObjectByField(MagicCardField.FLIPID, String.valueOf(card.getCardId()));
							magicDb.add(cardB);
						}
						String part1 = card.getBase().getProperty(MagicCardField.PART);
						String part = cardB.getBase().getProperty(MagicCardField.PART);
						if (part != null && part.length() > 0) {
							if (part.equals(part1))
								return; // second part updated - no support
							((ICardModifiable) card).setObjectByField(MagicCardField.OTHER_PART, part);
						}
						((ICardModifiable) card).setObjectByField(MagicCardField.FLIPID, String.valueOf(pairId));
					}
				}
			} else if (sides == 0) {
				MagicLogger.log("Problems parsing card " + card.getCardId());
				return;
			}
			if (nameOrig == null || !nameOrig.equals(nameTitle)) {
				MagicLogger.log("Name is not set: " + nameOrig + ", title " + nameTitle);
				return; // do not update if part is not matching
			}
			// extractField(card, fieldMapFilter, html, MagicCardField.NAME, cardAltPattern, true);
			// monitor.worked(1);
			extractField(card, fieldMapFilter, html, MagicCardField.RULINGS, rulingPattern, true);
			extractField(card, fieldMapFilter, html, MagicCardField.RATING, ratingPattern, false);
			extractField(card, fieldMapFilter, html, MagicCardField.ARTIST, artistPattern, false);
			extractField(card, fieldMapFilter, html, MagicCardField.COLLNUM, cardnumPattern, false);
			extractField(card, fieldMapFilter, html, MagicCardField.TYPE, typesPattern, false);
			monitor.worked(5);
			Matcher matcher = textPattern.matcher(html);
			if (matcher.find()) {
				String text = matcher.group(1);
				extractField(card, fieldMapFilter, text, isOracle() ? MagicCardField.ORACLE : MagicCardField.TEXT, textPatternEach, true);
			}
			monitor.worked(1);
			extractOtherSets(card, fieldMapFilter, html);
			monitor.worked(1);
			if (magicDb != null)
				magicDb.update(card.getBase());
		} finally {
			monitor.done();
		}
	}

	protected boolean isOracle() {
		return true;
	}

	@Override
	protected String getUrl() {
		String base = DETAILS_QUERY_URL_BASE + card.getCardId();
		String part = (String) card.getObjectByField(MagicCardField.PART);
		if (part == null) {
			return base;
		}
		if (part.contains("@"))
			return base;
		return base + "&part=" + part;
	}

	public void setMagicDb(ICardStore magicDb) {
		this.magicDb = magicDb;
	}

	public static void main(String[] args) throws IOException {
		MagicCard card = new MagicCard();
		card.setCardId(227415);
		// card.setCardId(191338);
		ParseGathererDetails parser = new ParseGathererDetails();
		parser.setCard(card);
		parser.load(ICoreProgressMonitor.NONE);
		System.err.println(card.getArtist() + " " + card.getCommunityRating() + " " + card.getCollNumber());
	}

	public void addFilter(MagicCardField field) {
		if (fieldMapFilter == null)
			fieldMapFilter = new HashSet<ICardField>();
		fieldMapFilter.add(field);
	}
}
