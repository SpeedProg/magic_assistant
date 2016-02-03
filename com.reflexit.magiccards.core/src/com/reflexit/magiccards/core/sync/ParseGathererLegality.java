/*******************************************************************************
 * Copyright (c) 2008 Alena Laskavaia.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package com.reflexit.magiccards.core.sync;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.legality.Format;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Legality;
import com.reflexit.magiccards.core.model.LegalityMap;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.abs.ICardSet;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.monitor.SubCoreProgressMonitor;

/**
 * Retrieve legality info
 */
public class ParseGathererLegality extends AbstractParseHtmlPage {
	private static final String LEGALITY_QUERY_URL_BASE = GatherHelper.GATHERER_URL_BASE
			+ "Pages/Card/Printings.aspx?multiverseid=";
	private int cardId;
	private IMagicCard card;

	@Override
	protected String getUrl() {
		return LEGALITY_QUERY_URL_BASE + getCardId();
	}

	/*-
	 <table class="cardList" cellspacing="0" cellpadding="2">
	<tr class="headerRow">
	    <td class="headerCell" style="text-align:left;">
	        Format
	    </td>
	    <td class="headerCell" style="text-align:center;">

	        Legality
	    </td>
	</tr>
	
	        <tr class="cardItem evenItem">
	            <td style="width:40%;">
	                Standard
	            </td>
	            <td style="text-align:center;">
	                Legal
	            </td>

	        </tr>
	    
	        <tr class="cardItem oddItem">
	            <td>
	                Extended
	            </td>
	            <td style="text-align:center;">
	                Legal
	            </td>
	        </tr>

	 */
	private static Pattern rowPattern = Pattern.compile(">\\s*([^<]*)\\s*</td>");
	private LegalityMap legalityMap;

	public synchronized LegalityMap getLegalityMap() {
		return legalityMap;
	}

	protected synchronized void setCardId(int cardId) {
		this.cardId = cardId;
	}

	protected synchronized int getCardId() {
		return cardId;
	}

	public LegalityMap getCardLegality(int id) throws IOException {
		return getCardLegality(id, ICoreProgressMonitor.NONE);
	}

	public LegalityMap getCardLegality(int id, ICoreProgressMonitor monitor) throws IOException {
		ParseGathererLegality parser = this;
		synchronized (parser) {
			parser.setCardId(id);
			parser.load(monitor);
			LegalityMap cardLegality = parser.getLegalityMap();
			return cardLegality;
		}
	}

	public static Map<Integer, LegalityMap> cardSetLegality(ICardSet<IMagicCard> cards,
			ICoreProgressMonitor monitor) throws IOException {
		monitor.beginTask("Updaing cards legality", cards.size() * 10);
		try {
			ParseGathererLegality parser = new ParseGathererLegality();
			Map<Integer, LegalityMap> res = new HashMap<Integer, LegalityMap>();
			IOException ex = null; // last exception
			for (IMagicCard magicCard : cards) {
				int id = magicCard.getGathererId();
				if (id == 0)
					continue;
				try {
					LegalityMap map = parser.getCardLegality(id, new SubCoreProgressMonitor(monitor, 10));
					res.put(id, map);
					MagicCard base = (magicCard.getBase());
					base.setLegalityMap(map);
				} catch (IOException e) {
					ex = e;
				}
			}
			if (res.size() == 0 && ex != null)
				throw ex;
			return res;
		} finally {
			monitor.done();
		}
	}

	@Override
	protected synchronized void loadHtml(String html, ICoreProgressMonitor monitor) {
		HashMap<Format, Legality> map = new HashMap();
		String lines[] = html.split("\n");
		int state = 0;
		String row = "";
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (line.contains("This card has restrictions in the following formats")) {
				state = 1;
				continue;
			}
			if (state == 0)
				continue;
			if (line.contains("<tr ")) {
				state = 2;
				row = line;
				continue;
			}
			if (line.contains("</tr>")) {
				state = 1;
				row += line;
				if (row.contains("headerRow"))
					continue;
				Matcher matcher = rowPattern.matcher(row);
				if (matcher.find()) {
					String format = matcher.group(1).trim();
					if (matcher.find()) {
						String legal = matcher.group(1).trim();
						map.put(Format.valueOf(format), Legality.fromLabel(legal));
					} else {
						MagicLogger.log("Cannot parse legality " + row);
					}
				}
				continue;
			}
			if (state == 2) {
				row += line;
			}
		}
		legalityMap = LegalityMap.valueOf(map).complete();
		if (card != null) {
			((MagicCard) card).setLegalityMap(legalityMap);
			card = null;
		}
	}

	public static void main(String[] args) throws IOException {
		int id = 193867;
		ParseGathererLegality parser = new ParseGathererLegality();
		parser.setCardId(id);
		parser.load(ICoreProgressMonitor.NONE);
		LegalityMap cardLegality = parser.getLegalityMap();
		System.err.println(cardLegality);
	}

	public void setCard(MagicCard card) {
		this.card = card;
		this.cardId = card.getCardId();
	}
}
