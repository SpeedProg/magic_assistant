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
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Editions.Edition;

/**
 * Retrieve legality info
 */
public class ParseGathererSets extends ParseGathererPage {
	private static final String SET_QUERY_URL_BASE = GATHERER_URL_BASE + "Pages/Default.aspx";
	/*-
	                <b>
	                    Filter Card Set:
	                </b>
	                <p>
	                    <select name="ctl00$ctl00$MainContent$Content$SearchControls$setAddText" id="ctl00_ctl00_MainContent_Content_SearchControls_setAddText">
	<option value=""></option>
	<option value="Alara Reborn">Alara Reborn</option>
	<option value="Alliances">Alliances</option>
	<option value="Antiquities">Antiquities</option>
	...
	</select>
	 */
	private static Pattern setStartPattern = Pattern.compile("<b>\\s*Filter Card Set:.*?<option value=\"\"></option>(.*?)</select>");
	private static Pattern oneSetPattern = Pattern.compile("<option.*?>(.*?)</option>");

	public ParseGathererSets() {
		setTitle("Updating sets...");
	}

	@Override
	protected void loadHtml(String html, IProgressMonitor monitor) {
		html = html.replaceAll("\r?\n", " ");
		Matcher matcher = setStartPattern.matcher(html);
		if (matcher.find()) {
			String sets = matcher.group(1);
			Matcher mset = oneSetPattern.matcher(sets);
			while (mset.find()) {
				String name = mset.group(1).trim();
				if (name.length() == 0)
					continue;
				name = name.replaceAll("&quot;", "\"");
				Editions.getInstance().addAbbr(name, null);
			}
		}
	}

	@Override
	protected String getUrl() {
		return SET_QUERY_URL_BASE;
	}

	public static void main(String[] args) throws IOException {
		// card.setCardId(11179);
		ParseGathererSets parser = new ParseGathererSets();
		parser.load(new NullProgressMonitor());
		Collection<Edition> editions = Editions.getInstance().getEditions();
		for (Iterator iterator = editions.iterator(); iterator.hasNext();) {
			Edition edition = (Edition) iterator.next();
			System.err.println(edition.getName() + " " + edition.getMainAbbreviation());
		}
	}
}
