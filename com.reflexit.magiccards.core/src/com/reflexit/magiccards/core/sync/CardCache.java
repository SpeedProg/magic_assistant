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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
	public static URL createCardURL(IMagicCard card) throws MalformedURLException {
		String edition = card.getEdition();
		String editionAbbr = Editions.getInstance().getAbbrByName(edition);
		if (editionAbbr == null)
			return null;
		int cardId = card.getCardId();
		String locale = guessLocale(cardId, edition, editionAbbr);
		if (locale == null || locale.length() == 0)
			return null;
		String file = getLocalImage(cardId, editionAbbr, locale);
		URL url = new URL("file:/" + file);
		if (new File(file).exists()) {
			return url;
		}
		URL remoteUrl = ParseGathererSpoiler.createImageURL(cardId, editionAbbr, locale);
		saveStream(remoteUrl, file);
		return url;
	}

	/**
	 * @param remoteUrl
	 * @param file
	 */
	private static void saveStream(URL remoteUrl, String file) {
		try {
			new File(file).getParentFile().mkdirs();
			OutputStream st = new FileOutputStream(file);
			InputStream openStream = remoteUrl.openStream();
			byte[] bytes = new byte[1024 * 4];
			int k;
			while ((k = openStream.read(bytes)) > 0) {
				st.write(bytes, 0, k);
			}
			st.close();
			openStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			// no file
		}
	}

	public static URL createImageURL(int cardId, String editionAbbr, String locale) throws MalformedURLException {
		return new URL("http://resources.wizards.com/Magic/Cards/" + editionAbbr + "/" + locale + "/Card" + cardId
		        + ".jpg");
	}

	public static String getLocalImage(int cardId, String editionAbbr, String locale) throws MalformedURLException {
		IPath path = Activator.getStateLocationAlways();
		String part = "Cards/" + editionAbbr + "/" + locale + "/Card" + cardId + ".jpg";
		String file = path.append(part).toPortableString();
		return file;
	}

	private static String guessLocale(int cardId, String edition, String editionAbbr) throws MalformedURLException {
		String locale = Editions.getInstance().getLocale(edition);
		if (locale == null) {
			URL url = null;
			locale = "en-us";
			url = ParseGathererSpoiler.createImageURL(cardId, editionAbbr, locale);
			try {
				url.openStream();
				Editions.getInstance().addLocale(editionAbbr, locale);
			} catch (IOException e) {
				locale = "EN";
				url = ParseGathererSpoiler.createImageURL(cardId, editionAbbr, locale);
				try {
					url.openStream();
					Editions.getInstance().addLocale(editionAbbr, locale);
				} catch (IOException e2) {
					locale = "";
					e2.printStackTrace();
				}
			}
			try {
				Editions.getInstance().save();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return locale;
	}
}
