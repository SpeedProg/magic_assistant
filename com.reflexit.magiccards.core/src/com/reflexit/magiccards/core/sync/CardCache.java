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

import org.eclipse.core.runtime.IPath;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.IMagicCard;

/**
 * @author Alena
 *
 */
public class CardCache {
	private static boolean caching;

	public static void setCahchingEnabled(boolean enabled) {
		caching = enabled;
	};

	public static URL createCardURL(IMagicCard card) throws IOException {
		String edition = card.getSet();
		String editionAbbr = Editions.getInstance().getAbbrByName(edition);
		if (editionAbbr == null)
			return null;
		int cardId = card.getCardId();
		String locale = Editions.getInstance().getLocale(edition);
		String file = createLocalImageFilePath(cardId, editionAbbr, locale);
		URL localUrl = new File(file).toURL();
		InputStream st = null;
		if (locale != null) {
			if (caching) {
				if (new File(file).exists()) {
					return localUrl;
				}
				try {
					st = tryLocale(cardId, locale, edition, editionAbbr);
				} catch (IOException e1) {
					throw e1;
				}
			}
		} else {
			try {
				try {
					st = tryLocale(cardId, "EN", edition, editionAbbr);
				} catch (FileNotFoundException e) {
					st = tryLocale(cardId, "en-us", edition, editionAbbr);
				}
			} catch (IOException e1) {
				throw e1;
			}
			locale = Editions.getInstance().getLocale(edition);
		}
		if (caching && st != null) {
			try {
				ImageCache.saveStream(file, st);
				st.close();
				return localUrl;
			} catch (IOException e) {
				Activator.log(e);
			}
		}
		URL remoteUrl = ParseGathererSpoiler.createImageURL(cardId, editionAbbr, locale == null ? "EN" : locale);
		return remoteUrl;
	}

	public static URL createImageURL(int cardId, String editionAbbr, String locale) throws MalformedURLException {
		return new URL("http://resources.wizards.com/Magic/Cards/" + editionAbbr + "/" + locale + "/Card" + cardId
		        + ".jpg");
	}

	public static String createLocalImageFilePath(int cardId, String editionAbbr, String locale)
	        throws MalformedURLException {
		IPath path = Activator.getStateLocationAlways();
		String part = "Cards/" + editionAbbr + "/" + locale + "/Card" + cardId + ".jpg";
		String file = path.append(part).toPortableString();
		return file;
	}

	private static InputStream tryLocale(int cardId, String locale, String edition, String editionAbbr)
	        throws MalformedURLException, IOException {
		String oldLocale = Editions.getInstance().getLocale(edition);
		URL url = ParseGathererSpoiler.createImageURL(cardId, editionAbbr, locale);
		InputStream st = url.openStream();
		if (oldLocale == null || !oldLocale.equals(locale)) {
			Editions.getInstance().addLocale(edition, locale);
			Editions.getInstance().save();
		}
		return st;
	}
}
