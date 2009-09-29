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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.storage.ICardSet;

/**
 * Retrieve legality info
 */
public class ParseGathererLegality {
	private static final String GATHERER_URL_BASE = "http://gatherer.wizards.com/";
	private static final String LEGALITY_QUERY_URL_BASE = GATHERER_URL_BASE + "Pages/Card/Printings.aspx?multiverseid=";
	private static Charset UTF_8 = Charset.forName("utf-8");
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

	public static Map<String, String> cardLegality(int id) throws IOException {
		HashMap<String, String> res = new LinkedHashMap<String, String>();
		URL url = new URL(LEGALITY_QUERY_URL_BASE + id);
		InputStream openStream = url.openStream();
		BufferedReader st = new BufferedReader(new InputStreamReader(openStream, UTF_8));
		String line;
		String row = "";
		int state = 0;
		while ((line = st.readLine()) != null) {
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
				{
					if (row.contains("headerRow"))
						continue;
					Matcher matcher = rowPattern.matcher(row);
					if (matcher.find()) {
						String format = matcher.group(1).trim();
						if (matcher.find()) {
							String legal = matcher.group(1).trim();
							res.put(format, legal);
						} else {
							System.err.println("? " + row);
						}
					}
				}
				continue;
			}
			if (state == 2) {
				row += line;
			}
		}
		st.close();
		return res;
	}

	public static Map<Integer, Map<String, String>> cardSetLegality(ICardSet<IMagicCard> cards) throws IOException {
		Map<Integer, Map<String, String>> res = new LinkedHashMap<Integer, Map<String, String>>();
		IOException ex = null; // last exception
		for (IMagicCard magicCard : cards) {
			int id = magicCard.getCardId();
			try {
				Map map = cardLegality(id);
				res.put(id, map);
			} catch (IOException e) {
				ex = e;
			}
		}
		if (res.size() == 0 && ex != null)
			throw ex;
		return res;
	}

	public static void main(String[] args) throws IOException {
		int id = 193867;
		Map<String, String> cardLegality = cardLegality(id);
		System.err.println(cardLegality);
	}
}
