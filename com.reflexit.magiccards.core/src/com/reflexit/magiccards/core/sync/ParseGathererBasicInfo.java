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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

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
public class ParseGathererBasicInfo extends ParseGathererPage {
	private static final String RULINGS_QUERY_URL_BASE = GATHERER_URL_BASE + "Pages/Card/Details.aspx?printed=true&multiverseid=";
	private IMagicCard card;
	private Set<ICardField> fieldMapFilter;
	private ICardStore magicDb;
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
	      <div class="cardtextbox">When Anathemancer enters the battlefield, it deals damage to target player equal to the number of nonbasic lands that player controls.</div>
	      <div class="cardtextbox">Unearth <img src="/Handlers/Image.ashx?size=small&amp;name=5&amp;type=symbol" alt="5" align="absbottom" /><img src="/Handlers/Image.ashx?size=small&amp;name=B&amp;type=symbol" alt="Black" align="absbottom" /><img src="/Handlers/Image.ashx?size=small&amp;name=R&amp;type=symbol" alt="Red" align="absbottom" /> <i>(<img src="/Handlers/Image.ashx?size=small&amp;name=5&amp;type=symbol" alt="5" align="absbottom" /><img src="/Handlers/Image.ashx?size=small&amp;name=B&amp;type=symbol" alt="Black" align="absbottom" /><img src="/Handlers/Image.ashx?size=small&amp;name=R&amp;type=symbol" alt="Red" align="absbottom" />: Return this card from your graveyard to the battlefield. It gains haste. Exile it at the beginning of the next end step or if it would leave the battlefield. Unearth only as a sorcery.)</i></div></div>

	 */
	private static Pattern textPattern = Pattern.compile("<div class=\"cardtextbox\">(.*?)</div>");
	/*-
	     <div class="contentTitle">

	     <span id="ctl00_ctl00_ctl00_MainContent_SubContent_SubContentHeader_subtitleDisplay">Книга Заклинаний</span>
	     </div>
	 */
	private static Pattern namePattern = Pattern.compile("<div class=\"contentTitle\">\\s*<span id.*?>(.*?)</span>");

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
				if (field == MagicCardField.TEXT) {
					// hack to correct weird values
					value = value.replaceAll("o([WGUBR0-9])", "{$1}");
					value = value.replaceAll("ocT", "{T}");
				}
				if (field == MagicCardField.TEXT || field == MagicCardField.ORACLE) {
					value = value.replaceAll("\\n", "<br>");
					value = ParseGathererNewVisualSpoiler.htmlToString(value);
				}
				if (field == MagicCardField.TYPE) {
					value = value.replaceAll("—", "-");
					// value = value.replaceAll("—", "-");
					value = value.replaceAll(": -", ":");
				}
				((ICardModifiable) card).setObjectByField(field, value);
			}
		}
	}

	public void updateCard(IMagicCard magicCard, Set<ICardField> fieldMap, IProgressMonitor monitor) throws IOException {
		try {
			ICardHandler cardHandler = DataManager.getCardHandler();
			if (cardHandler != null)
				setMagicDb(cardHandler.getMagicDBStore());
			parseSingleCard(magicCard, fieldMap, monitor);
		} catch (IOException e) {
			Activator.log("Cannot load card " + e.getMessage() + " " + magicCard.getCardId());
		}
	}

	void parseSingleCard(IMagicCard magicCard, Set<ICardField> fieldMap, IProgressMonitor monitor) throws IOException {
		setCard(magicCard);
		setFilter(fieldMap);
		load(monitor);
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
			extractField(card, fieldMapFilter, html, MagicCardField.NAME, namePattern);
			monitor.worked(1);
			extractField(card, fieldMapFilter, html, MagicCardField.TYPE, typesPattern);
			monitor.worked(1);
			extractField(card, fieldMapFilter, html, MagicCardField.TEXT, textPattern);
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
		card.setCardId(172550);
		// card.setCardId(191338);
		ParseGathererBasicInfo parser = new ParseGathererBasicInfo();
		parser.setCard(card);
		parser.load(new NullProgressMonitor());
		System.out.println(card.getName());
		System.out.println(card.getType());
		System.out.println(card.getOracleText());
	}
}
