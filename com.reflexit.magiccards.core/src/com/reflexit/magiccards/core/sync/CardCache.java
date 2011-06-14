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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IPath;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.IMagicCard;

/**
 * @author Alena
 * 
 */
public class CardCache {
	private static boolean caching;
	private static boolean loading;

	public static void setCahchingEnabled(boolean enabled) {
		caching = enabled;
	};

	public static void setLoadingEnabled(boolean enabled) {
		loading = enabled;
	};

	public static URL createCardURL(IMagicCard card) throws IOException {
		return createCardURL(card, isLoadingEnabled(), caching);
	}

	public static URL createCardURL(IMagicCard card, boolean remote, boolean cacheImage) throws MalformedURLException, IOException {
		String edition = card.getSet();
		String editionAbbr = Editions.getInstance().getAbbrByName(edition);
		if (editionAbbr == null)
			return null;
		int cardId = card.getCardId();
		String file = createLocalImageFilePath(cardId, editionAbbr);
		URL localUrl = new File(file).toURL();
		InputStream st = null;
		if (cacheImage) {
			if (new File(file).exists()) {
				return localUrl;
			}
			try {
				st = tryUrl(cardId, edition, editionAbbr, remote);
			} catch (IOException e1) {
				throw e1;
			}
		}
		if (cacheImage && st != null) {
			try {
				ImageCache.saveStream(file, st);
				st.close();
				return localUrl;
			} catch (IOException e) {
				Activator.log(e);
			}
		}
		URL remoteUrl = createImageURL(cardId, editionAbbr, remote);
		return remoteUrl;
	}

	public static URL createSetImageURL(IMagicCard card, boolean upload) throws IOException {
		String edition = card.getSet();
		String editionAbbr = Editions.getInstance().getAbbrByName(edition);
		String rarity = card.getRarity();
		if (editionAbbr == null)
			return null;
		String file = createLocalSetImageFilePath(editionAbbr, rarity);
		URL localUrl = new File(file).toURL();
		if (upload == false)
			return localUrl;
		if (new File(file).exists()) {
			return localUrl;
		}
		try {
			URL url = createSetImageRemoteURL(editionAbbr, rarity);
			if (url == null)
				return null;
			InputStream st = url.openStream();
			ImageCache.saveStream(file, st);
			st.close();
		} catch (IOException e1) {
			throw e1;
		}
		return localUrl;
	}

	public static URL createImageURL(int cardId, String editionAbbr, boolean remote) throws MalformedURLException {
		if (!remote)
			return null;
		return ParseGathererNewVisualSpoiler.createImageURL(cardId, editionAbbr);
	}

	public static URL createSetImageRemoteURL(String editionAbbr, String rarity) throws MalformedURLException {
		if (!CardCache.isLoadingEnabled())
			return null;
		return ParseGathererNewVisualSpoiler.createSetImageURL(editionAbbr, rarity);
	}

	public static String createLocalImageFilePath(int cardId, String editionAbbr) {
		IPath path = Activator.getStateLocationAlways();
		if (editionAbbr.equals("CON")) {
			// special hack for windows, which cannot create CON directory
			editionAbbr = "CONFL";
		}
		String locale = "EN";
		String part = "Cards/" + editionAbbr + "/" + locale + "/Card" + cardId + ".jpg";
		String file = path.append(part).toPortableString();
		return file;
	}

	public static String createLocalImageFilePath(IMagicCard card) {
		String edition = card.getSet();
		String editionAbbr = Editions.getInstance().getAbbrByName(edition);
		if (editionAbbr == null)
			return null;
		int cardId = card.getCardId();
		return createLocalImageFilePath(cardId, editionAbbr);
	}

	public static String createLocalSetImageFilePath(String editionAbbr, String rarity) throws MalformedURLException {
		IPath path = Activator.getStateLocationAlways();
		String part = "Sets/" + editionAbbr + "-" + rarity + ".jpg";
		String file = path.append(part).toPortableString();
		return file;
	}

	private static InputStream tryUrl(int cardId, String edition, String editionAbbr, boolean remote) throws MalformedURLException,
			IOException {
		URL url = createImageURL(cardId, editionAbbr, remote);
		if (url == null)
			return null;
		try {
			InputStream st = url.openStream();
			return st;
		} catch (IOException e) {
			throw new IOException("Cannot connect: " + e.getMessage());
		}
	}

	public static boolean isLoadingEnabled() {
		return loading;
	}
}
