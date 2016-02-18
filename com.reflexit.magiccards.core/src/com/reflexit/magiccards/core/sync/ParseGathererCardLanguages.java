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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;

/**
 * Retrieve legality info
 */
public class ParseGathererCardLanguages extends AbstractParseHtmlPage {
	static final String SET_QUERY_URL_BASE = GatherHelper.GATHERER_URL_BASE + "Pages/Card/Languages.aspx";
	private String lang;
	private int cardId;
	private List<Integer> langIds = new ArrayList<>();
	private int page;
	/*-
	        <tr class="cardItem oddItem">
	            <td class="fullWidth" style="text-align: center;">
	                <a id="ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_languageList_listRepeater_ctl07_cardTitle" href="Details.aspx?multiverseid=172550">Бурав Выжженной Пустоши</a>
	            </td>
	            <td style="text-align: center;">
	                Russian
	            </td>
	            <td style="text-align: center;">

	                русский язык
	            </td>
	        </tr>
	 */
	private static Pattern rowPattern = Pattern
			.compile("<tr class=\"cardItem(.*?multiverseid=(\\d+).*?)</tr>");



	public void setLanguage(String string) {
		lang = string;
		page = 0;
	}

	public void setCardId(int i) {
		cardId = i;
		page = 0;
	}

	@Override
	protected void loadHtml(String html1, ICoreProgressMonitor monitor) {
		String html = html1.replaceAll("\r?\n", " ");
		Matcher matcher = rowPattern.matcher(html);
		if (page == 0)
			langIds.clear();
		int count = 0;
		boolean last = false;
		while (matcher.find()) {
			last = false;
			count++;
			String all = matcher.group(1);
			String id = matcher.group(2);
			if (all.contains(lang)) {
				Integer langId1 = Integer.valueOf(id);
				langIds.add(langId1);
				last = true;
			}
		}
		if (langIds.size() == 0 || last)
			if (count >= 25 && page <= 3) {
				page++;
				try {
					load(ICoreProgressMonitor.NONE);
				} catch (IOException e) {
					// ignore
		}
	}
	}

	@Override
	protected String getUrl() {
		return SET_QUERY_URL_BASE + "?page=" + page + "&multiverseid=" + cardId;
	}

	public int getLangCardId() {
		return langIds.get(0);
	}

	public List<Integer> getLangCardIds() {
		return langIds;
	}

	public static void main(String[] args) throws IOException {
		ParseGathererCardLanguages parser = new ParseGathererCardLanguages();
		parser.setCardId(407693);
		parser.setLanguage("Portuguese");
		parser.load(ICoreProgressMonitor.NONE);
		System.err.println(parser.getLangCardIds());
	}
}
