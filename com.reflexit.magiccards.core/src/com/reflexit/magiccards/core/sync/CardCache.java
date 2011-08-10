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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.CachedImageNotFoundException;
import com.reflexit.magiccards.core.CannotDetermineSetAbbriviation;
import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.NotNull;
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

	public static URL createSetImageURL(IMagicCard card, boolean upload) throws IOException {
		String edition = card.getSet();
		String editionAbbr = Editions.getInstance().getAbbrByName(edition);
		String rarity = card.getRarity();
		if (editionAbbr == null)
			return null;
		String path = createLocalSetImageFilePath(editionAbbr, rarity);
		File file = new File(path);
		URL localUrl = file.toURI().toURL();
		if (upload == false)
			return localUrl;
		if (file.exists()) {
			return localUrl;
		}
		try {
			URL url = createSetImageRemoteURL(editionAbbr, rarity);
			if (url == null)
				return null;
			InputStream st = url.openStream();
			FileUtils.saveStream(st, file);
			st.close();
		} catch (IOException e1) {
			throw e1;
		}
		return localUrl;
	}

	public static URL createRemoteImageURL(IMagicCard card) throws MalformedURLException {
		String edition = card.getSet();
		String editionAbbr = Editions.getInstance().getAbbrByName(edition);
		if (editionAbbr == null)
			throw new CannotDetermineSetAbbriviation(card);
		return ParseGathererNewVisualSpoiler.createImageURL(card.getCardId(), editionAbbr);
	}

	public static URL createSetImageRemoteURL(String editionAbbr, String rarity) throws MalformedURLException {
		if (!CardCache.isLoadingEnabled())
			return null;
		return ParseGathererNewVisualSpoiler.createSetImageURL(editionAbbr, rarity);
	}

	@NotNull
	public static String createLocalImageFilePath(IMagicCard card) {
		String edition = card.getSet();
		String editionAbbr = Editions.getInstance().getAbbrByName(edition);
		if (editionAbbr == null)
			throw new CannotDetermineSetAbbriviation(card);
		int cardId = card.getCardId();
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

	public static String createLocalSetImageFilePath(String editionAbbr, String rarity) throws MalformedURLException {
		IPath path = Activator.getStateLocationAlways();
		String part = "Sets/" + editionAbbr + "-" + rarity + ".jpg";
		String file = path.append(part).toPortableString();
		return file;
	}

	public static boolean isLoadingEnabled() {
		return loading;
	}

	private static LinkedHashMap<IMagicCard, Boolean> cardImageQueue = new LinkedHashMap<IMagicCard, Boolean>();
	private static Job cardImageLoadingJob = new Job("Loading card images") {
		{
			setSystem(true);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			while (true) {
				IMagicCard card = null;
				boolean forceUpdate = false;
				synchronized (cardImageQueue) {
					if (cardImageQueue.size() > 0) {
						card = cardImageQueue.keySet().iterator().next();
						forceUpdate = cardImageQueue.get(card);
						cardImageQueue.remove(card);
					} else
						return Status.OK_STATUS;
				}
				synchronized (card) {
					try {
						downloadAndSaveImage(card, isLoadingEnabled(), forceUpdate);
					} catch (Exception e) {
						continue;
					} finally {
						card.notifyAll();
					}
				}
			}
		}
	};

	public static void queueImageLoading(IMagicCard card, boolean forceUpdate) {
		synchronized (cardImageQueue) {
			cardImageQueue.put(card, forceUpdate);
		}
		cardImageLoadingJob.schedule(0);
	}

	/**
	 * Download and save card image, if not already saved
	 * 
	 * @param card
	 * @param caching2
	 * @return
	 * @throws IOException
	 */
	public static File downloadAndSaveImage(IMagicCard card, boolean remote, boolean forceRemote) throws IOException {
		synchronized (card) {
			String path = CardCache.createLocalImageFilePath(card);
			File file = new File(path);
			if (forceRemote == false && file.exists()) {
				return file;
			}
			if (!remote)
				throw new CachedImageNotFoundException("Cannot find cached image for " + card.getName());
			URL url = createRemoteImageURL(card);
			InputStream st = null;
			try {
				st = url.openStream();
			} catch (IOException e) {
				throw new IOException("Cannot connect: " + e.getMessage());
			}
			File file2 = new File(path + ".part");
			FileUtils.saveStream(st, file2);
			st.close();
			if (file2.exists()) {
				file2.renameTo(file);
				return file;
			}
			throw new FileNotFoundException(file.toString());
		}
	}

	/**
	 * Get card image or schedule a loading job if image not found. This image
	 * is not managed - to be disposed by called.
	 * 
	 * @param card
	 * @throws IOException
	 */
	public static void loadCardImageOffline(IMagicCard card, boolean forceUpdate) throws IOException, CannotDetermineSetAbbriviation {
		String path = createLocalImageFilePath(card);
		File file = new File(path);
		if (file.exists() && forceUpdate == false) {
			return;
		}
		CardCache.queueImageLoading(card, forceUpdate);
		return;
	}
}
