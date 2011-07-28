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
public class ParseGathererSets {
	private static final String GATHERER_URL_BASE = "http://gatherer.wizards.com/";
	private static final String SET_QUERY_URL_BASE = GATHERER_URL_BASE + "Pages/Default.aspx";
	private static Charset UTF_8 = Charset.forName("utf-8");
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

	public static void loadEditions(IProgressMonitor monitor) throws IOException {
		monitor.beginTask("Updating sets", 100);
		try {
			URL url = new URL(SET_QUERY_URL_BASE);
			InputStream openStream = url.openStream();
			BufferedReader st = new BufferedReader(new InputStreamReader(openStream, UTF_8));
			String line;
			String html = "";
			while ((line = st.readLine()) != null) {
				html += line + " ";
				if (monitor.isCanceled())
					return;
			}
			st.close();
			monitor.worked(90);
			if (monitor.isCanceled())
				return;
			loadEditions(html);
		} finally {
			monitor.done();
		}
	}

	private static void loadEditions(String html) {
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

	public static void main(String[] args) throws IOException {
		// card.setCardId(11179);
		ParseGathererSets parser = new ParseGathererSets();
		parser.loadEditions(new NullProgressMonitor());
		Collection<Edition> editions = Editions.getInstance().getEditions();
		for (Iterator iterator = editions.iterator(); iterator.hasNext();) {
			Edition edition = (Edition) iterator.next();
			System.err.println(edition.getName() + " " + edition.getMainAbbreviation());
		}
	}
}
