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
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.ICardHandler;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.abs.ICardField;
import com.reflexit.magiccards.core.model.abs.ICardGroup;
import com.reflexit.magiccards.core.model.abs.ICardModifiable;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.MemoryCardStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.monitor.SubCoreProgressMonitor;
import com.reflexit.magiccards.core.xml.StringCache;

/**
 * Retrieve legality info
 */
public class ParseGathererOracle extends AbstractParseHtmlPage {
	public static final String DETAILS_QUERY_URL_BASE = GatherHelper.GATHERER_URL_BASE
			+ "Pages/Card/Details.aspx?multiverseid=";
	private MagicCard fromCard;
	private Set<ICardField> fieldMapFilter;
	private ICardStore<IMagicCard> magicDb;
	protected ICardStore<MagicCard> sets;
	protected MagicCard cardA;
	protected MagicCard cardB;
	private String resultTitle;
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
	private static Pattern artistPattern = Pattern
			.compile("ArtistCredit\"\\sclass=\"value\">.*?\">(.*?)</a>");
	/*-
	      <div id="ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_numberRow" class="row">
	                        <div class="label">
	                            Card #:</div>
	                        <div class="value">
	                            33</div>
	                    </div>

	 */
	private static Pattern cardnumPattern = Pattern
			.compile("Card Number:</div>\\s*<div class=\"value\">\\s*(.*?)</div>");
	private static Pattern colorIdPattern = Pattern
			.compile("Color Indicator:</div>\\s*<div class=\"value\">\\s*(.*?)</div>");
	/*-
	      <div class="cardtextbox">When Anathemancer enters the battlefield, it deals damage to target player equal to the number of nonbasic lands that player controls.</div>
	      <div class="cardtextbox">Unearth <img src="/Handlers/Image.ashx?size=small&amp;name=5&amp;type=symbol" alt="5" align="absbottom" /><img src="/Handlers/Image.ashx?size=small&amp;name=B&amp;type=symbol" alt="Black" align="absbottom" /><img src="/Handlers/Image.ashx?size=small&amp;name=R&amp;type=symbol" alt="Red" align="absbottom" /> <i>(<img src="/Handlers/Image.ashx?size=small&amp;name=5&amp;type=symbol" alt="5" align="absbottom" /><img src="/Handlers/Image.ashx?size=small&amp;name=B&amp;type=symbol" alt="Black" align="absbottom" /><img src="/Handlers/Image.ashx?size=small&amp;name=R&amp;type=symbol" alt="Red" align="absbottom" />: Return this card from your graveyard to the battlefield. It gains haste. Exile it at the beginning of the next end step or if it would leave the battlefield. Unearth only as a sorcery.)</i></div></div>

	 */
	private static Pattern textPattern = Pattern.compile("Card Text:</div>(.*?)<div class=\"label\"");
	private static Pattern textPatternEach = Pattern.compile("<div class=\"cardtextbox\"[^>]*>(.*?)</div>");
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
	private static Pattern otherSetPattern = Pattern
			.compile("Sets:</div>\\s*<div class=\"value\">\\s*(.*?)</div>");
	private static Pattern expansionPattern = Pattern
			.compile("Expansion:</div>.*?set=\\[.*?\\].*?\">(.*?)</a>");
	private static Pattern otherSetPatternEach = Pattern
			.compile("multiverseid=(\\d+)\"><img title=\"(.*?) \\((.*?)\\)");
	/*-

	<div id="ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_typeRow" class="row">
	    <div class="label">
	        Types:</div>

	    <div class="value">
	        Creature  — Elemental Warrior</div>
	</div>


	 */
	private static Pattern typesPattern = Pattern
			.compile("Types:</div>\\s*<div class=\"value\">\\s*(.*?)</div>");
	/*-
	 <div class="contentTitle">

	 <span id="ctl00_ctl00_ctl00_MainContent_SubContent_SubContentHeader_subtitleDisplay">Книга Заклинаний</span>
	 </div>
	 */
	private static Pattern titleNamePattern = Pattern
			.compile("<div class=\"contentTitle\">\\s*<span id.*?>(.*?)</span>");
	/*-
	 * <div class="label">Card Name:</div>
	   <div class="value">Instigator Gang</div>
	 */
	private static Pattern cardNamePattern = Pattern
			.compile("Card Name:</div>\\s*<div class=\"value\">\\s*(.*?)</div>");
	/*-
	 *              <img src="../../Handlers/Image.ashx?multiverseid=241988&amp;type=card"
	 *              id="ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_cardImage"
	 *              alt="Hinterland Harbor" style="border:none;" />

	 */
	private static Pattern cardIdPattern = Pattern.compile("multiverseid=(\\d+)&amp;type=card");
	private static Pattern cardRotatePattern = Pattern
			.compile("multiverseid=\\d+&amp;type=card&amp;(options=\\w+)");

	void parseSingleCard(IMagicCard card, Set<ICardField> fieldMap, ICoreProgressMonitor monitor)
			throws IOException {
		setCard(card);
		setFilter(fieldMap);
		load(monitor);
	}

	public void updateCard(IMagicCard magicCard, Set<ICardField> fieldMap, ICoreProgressMonitor monitor)
			throws IOException {
		ICardHandler cardHandler = DataManager.getCardHandler();
		if (cardHandler != null)
			setMagicDb(cardHandler.getMagicDBStore());
		parseSingleCard(magicCard, fieldMap, monitor);
	}

	protected void extractOtherSets(MagicCard card, Set<ICardField> fieldMap, String html) {
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
				MagicCard card2 = card.cloneCard();
				if (resultTitle != null)
					card2.setName(resultTitle);
				card2.setId(id);
				card2.setSet(set.trim());
				card2.setRarity(rarity.trim());
				card2.setLanguage(null);
				card2.setText(card.getOracleText());
				card2.setLegalityMap(null);
				sets.add(card2);
			}
		}
	}

	protected void extractField(MagicCard card, Set<ICardField> fieldMap, String html, MagicCardField field,
			Pattern pattern,
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
					value = GatherHelper.htmlToString(value);
				} else if (field == MagicCardField.COST) {
					value = GatherHelper.htmlToString(value);
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
				} else if (field == MagicCardField.COLLNUM) {
					return; // do not change
				}
			}
			if (value != null)
				value = value.trim();
			((ICardModifiable) card).set(field, StringCache.intern(value));
		}
	}

	public void setCard(IMagicCard card) {
		this.fromCard = card.getBase();
		this.sets = new MemoryCardStore<MagicCard>();
		this.resultTitle = null;
	}

	public void setFilter(Set<ICardField> fieldFilter) {
		fieldMapFilter = fieldFilter;
		if (fieldMapFilter == null) {
			fieldMapFilter = new HashSet<ICardField>();
			for (ICardField field : MagicCardField.allNonTransientFields(false)) {
				fieldMapFilter.add(field);
			}
		}
	}

	@Override
	protected void loadHtml(String htmlIn, ICoreProgressMonitor monitor) {
		monitor.beginTask("Updating card", 100);
		try {
			if (fromCard.getGathererId() == 0 || fromCard instanceof ICardGroup)
				return;
			loadAll(htmlIn, new SubCoreProgressMonitor(monitor, 50));
			applyToDb(new SubCoreProgressMonitor(monitor, 50));
		} finally {
			monitor.done();
		}
	}

	private void applyToDb(ICoreProgressMonitor monitor) {
		setFilter(fieldMapFilter);
		adjustUpdateFields();
		if (fieldMapFilter.isEmpty())
			return;
		if (someWhatMatches(fromCard, cardA, cardB)) {
			applyFromCard(cardA, fromCard);
			addNew(cardB);
		} else if (someWhatMatches(fromCard, cardB, cardA)) {
			applyFromCard(cardB, fromCard);
			addNew(cardA);
		} else {
			MagicLogger.log("Problems parsing card - id mismatch " + fromCard.getCardId() + " "
					+ cardA.getCardId());
			return;
		}
		if (magicDb != null) {
			for (Iterator<MagicCard> iterator2 = sets.iterator(); iterator2.hasNext();) {
				IMagicCard card2 = iterator2.next();
				addNew(card2);
			}
		}
	}

	private boolean addNew(IMagicCard card2) {
		if (magicDb != null && card2 != null)
			if (magicDb.getCard(card2.getCardId()) == null) {
				magicDb.add(card2);
				MagicLogger.log("Added " + card2);
				return true;
			}
		return false;
	}

	private boolean someWhatMatches(MagicCard from, MagicCard inet, MagicCard inet2) {
		if (inet == null)
			return false;
		int id = from.getCardId();
		if (id != inet.getCardId())
			return false;
		if (inet2 == null) // no second card
			return true;
		if (inet.getGathererId() != inet2.getGathererId())
			return true; // ids are diffrent for pairs, so it is equals to first
							// - good enought
		// collector number
		if (from.getSide() == inet.getSide())
			return true;
		// ids are the same
		String nameOrig = from.getName();
		if (nameOrig != null) {
			if (nameOrig.equals(inet.getName()))
				return true;
		} else {
			return true; // if no name - nothing to work with
		}
		return false;
	}

	private boolean adjustUpdateFields() {
		boolean noUpdateBasic = (fromCard.getBase().get(MagicCardField.NOUPDATE) != null);
		fieldMapFilter.remove(MagicCardField.ID);
		if (noUpdateBasic) {
			fieldMapFilter.remove(MagicCardField.NAME);
			fieldMapFilter.remove(MagicCardField.TYPE);
			fieldMapFilter.remove(MagicCardField.TEXT);
			fieldMapFilter.remove(MagicCardField.ORACLE);
			fieldMapFilter.remove(MagicCardField.FLIPID);
			fieldMapFilter.remove(MagicCardField.PART);
		}
		boolean noEnglish = fromCard.getBase().getEnglishCardId() != 0;
		if (isOracle()) {
			if (noEnglish) {
				fieldMapFilter.remove(MagicCardField.NAME);
				fieldMapFilter.remove(MagicCardField.TYPE);
			}
			fieldMapFilter.remove(MagicCardField.TEXT);
		} else {
			fieldMapFilter.remove(MagicCardField.ORACLE);
		}
		if (resultTitle != null && resultTitle.contains("//")) {
			fieldMapFilter.remove(MagicCardField.NAME);
		}
		return noUpdateBasic;
	}

	private void applyFromCard(MagicCard card, MagicCard toCard) {
		toCard.setEmptyFromCard(card);
		toCard.setNonEmptyFromCard(fieldMapFilter, card);
		if (magicDb != null) {
			if (!addNew(toCard))
				magicDb.update(toCard, fieldMapFilter);
		}
	}

	private void loadAll(String htmlIn, ICoreProgressMonitor monitor) {
		monitor.beginTask("Parsing card", 100);
		String html0 = htmlIn;
		html0 = html0.replace('\r', ' ');
		html0 = html0.replace('\n', ' ');
		if (html0.contains("’")) {
			html0 = html0.replace('’', '\'');
		}
		monitor.worked(30);
		resultTitle = extractPatternValue(html0, titleNamePattern, false);
		Matcher matcher0 = singleCardPattern.matcher(html0);
		ArrayList<MagicCard> sides = new ArrayList<MagicCard>();
		while (matcher0.find()) {
			String html = matcher0.group(1).trim();
			sides.add(loadSide(html));
		}
		if (sides.size() == 0) {
			throw new MagicException("Problems parsing card " + fromCard.getGathererId());
		}
		Iterator<MagicCard> iterator = sides.iterator();
		cardA = iterator.next();
		cardB = iterator.hasNext() ? iterator.next() : null;
		correctSides();
		monitor.worked(40);
		extractOtherSets(cardA, fieldMapFilter, html0);
		monitor.done();
	}

	protected void correctSides() {
		if (resultTitle == null)
			resultTitle = cardA.getName();
		if (resultTitle == null || cardA.getName() == null || cardA.getCardId() == 0)
			throw new MagicException("Major problems parsing card " + fromCard.getCardId());
		if (cardB == null)
			return;
		if (cardB.getName() == null || cardB.getCardId() == 0)
			throw new MagicException("Major problems parsing card " + fromCard.getCardId());
		if (resultTitle.indexOf('(') > 0) {
			resultTitle = resultTitle.substring(0, resultTitle.indexOf('(') - 1);
		}
		if (resultTitle.contains("//")) {
			// split cards, lets make cannonic name for it
			String parts[] = resultTitle.split(" // ");
			if (isOracle() && cardA.getName().equals(parts[1].trim())) {
				// parts are reversed - bug in gatherer
				MagicCard tmp = cardA;
				cardA = cardB;
				cardB = tmp;
				int num = cardA.getCollectorNumberId();
				cardA.setCollNumber(num + "a");
				cardB.setCollNumber(num + "b");
			}
			cardA.setName(resultTitle + " (" + parts[0].trim() + ")");
			cardB.setName(resultTitle + " (" + parts[1].trim() + ")");
		}
		if (cardA.getCardId() == cardB.getCardId()) {
			cardB.setCardId(-cardB.getCardId());
		}
		cardB.setNonEmpty(MagicCardField.FLIPID, cardA.getCardId());
		cardA.setNonEmpty(MagicCardField.FLIPID, cardB.getCardId());
	}

	protected MagicCard loadSide(String html) {
		MagicCard card = new MagicCard();
		card.setNonEmptyFromCard(fromCard);
		card.set(MagicCardField.PART, null);
		card.set(MagicCardField.FLIPID, null);
		Set<ICardField> fieldMap = null;
		extractField(card, fieldMap, html, MagicCardField.ID, cardIdPattern, false);
		extractField(card, fieldMap, html, MagicCardField.NAME, cardNamePattern, true);
		extractField(card, fieldMap, html, MagicCardField.RULINGS, rulingPattern, true);
		extractField(card, fieldMap, html, MagicCardField.SET, expansionPattern, false);
		extractField(card, fieldMap, html, MagicCardField.RATING, ratingPattern, false);
		extractField(card, fieldMap, html, MagicCardField.ARTIST, artistPattern, false);
		extractField(card, fieldMap, html, MagicCardField.COLLNUM, cardnumPattern, false);
		extractField(card, fieldMap, html, MagicCardField.COLOR_INDICATOR, colorIdPattern, false);
		extractField(card, fieldMap, html, MagicCardField.TYPE, typesPattern, false);
		extractField(card, fieldMap, html, MagicCardField.PART, cardRotatePattern, false);
		Matcher matcher = textPattern.matcher(html);
		if (matcher.find()) {
			String text = matcher.group(1);
			extractField(card, fieldMap, text, isOracle() ? MagicCardField.ORACLE : MagicCardField.TEXT,
					textPatternEach, true);
		}
		return card;
	}

	protected boolean isOracle() {
		return true;
	}

	@Override
	protected String getUrl() {
		String base = DETAILS_QUERY_URL_BASE + fromCard.getGathererId();
		String part = (String) fromCard.get(MagicCardField.PART);
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
		ParseGathererOracle parser = new ParseGathererOracle();
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
