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
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.reflexit.magiccards.core.CachedImageNotFoundException;
import com.reflexit.magiccards.core.CannotDetermineSetAbbriviation;
import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.NotNull;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Editions.Edition;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;

/**
 * @author Alena
 * 
 */
public class CardCache {
	private static boolean caching;
	private static boolean loading;

	public static void setCahchingEnabled(boolean enabled) {
		caching = enabled;
	}

	public static void setLoadingEnabled(boolean enabled) {
		loading = enabled;
	}

	public static URL createSetImageURL(IMagicCard card, boolean upload) throws IOException {
		String edition = card.getSet();
		String rarity = card.getRarity();
		return createSetImageURL(edition, rarity, upload);
	}

	public static URL createSetImageURL(String edition, String rarity, boolean upload) throws MalformedURLException, IOException {
		String editionAbbr = Editions.getInstance().getAbbrByName(edition);
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
		if (WebUtils.isWorkOffline())
			return null;
		try {
			URL url = createSetImageRemoteURL(editionAbbr, rarity);
			InputStream st = WebUtils.openUrl(url);
			FileUtils.saveStream(st, file);
			st.close();
		} catch (IOException e1) {
			throw e1;
		}
		return localUrl;
	}

	public static URL createRemoteImageURL(IMagicCard card) throws MalformedURLException {
		String strUrl = (String) card.get(MagicCardField.IMAGE_URL);
		if (strUrl == null)
			return null;
		return new URL(strUrl);
	}

	public static URL createSetImageRemoteURL(String editionAbbr, String rarity) {
		return GatherHelper.createSetImageURL(editionAbbr, rarity);
	}

	@NotNull
	public static String createLocalImageFilePath(IMagicCard card) {
		int cardId = card.getCardId();
		Edition set = Editions.getInstance().getEditionByName(card.getSet());
		if (set == null)
			MagicLogger.log("Cannot determine set for " + card.getSet());
		return createLocalImageFilePath(cardId, set == null ? null : set.getMainAbbreviation());
	}

	@NotNull
	public static String createLocalImageFilePath(int cardId, String abbr) {
		String editionAbbr = abbr;
		if (abbr == null)
			editionAbbr = "unknown";
		else if (abbr.equals("CON")) // special hack for windows, which cannot create CON directory
			editionAbbr = "CONFL";
		String part;
		if (cardId == 0) {
			part = "Cards/0.jpg";
		} else {
			part = "Cards/" + editionAbbr + "/" + "EN" + "/Card" + cardId + ".jpg"; // XXX remove EN
		}
		return new File(FileUtils.getStateLocationFile(), part).getPath();
	}

	public static String createLocalSetImageFilePath(String editionAbbr, String rarity) {
		File loc = FileUtils.getStateLocationFile();
		String part = "Sets/" + editionAbbr + "-" + rarity + ".jpg";
		String file = new File(loc, part).getPath();
		return file;
	}

	public static boolean isLoadingEnabled() {
		return loading;
	}

	private static ArrayList<IMagicCard> cardImageQueue = new ArrayList<IMagicCard>();
	private static Job cardImageLoadingJob = new Job("Loading card images") {
		{
			setSystem(true);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			while (true) {
				IMagicCard card = null;
				synchronized (cardImageQueue) {
					if (cardImageQueue.size() > 0) {
						card = cardImageQueue.iterator().next();
						cardImageQueue.remove(card);
					} else
						return Status.OK_STATUS;
				}
				synchronized (card) {
					try {
						downloadAndSaveImage(card, isLoadingEnabled(), true);
					} catch (Exception e) {
						continue;
					} finally {
						card.notifyAll();
					}
				}
			}
		}
	};

	private static void queueImageLoading(IMagicCard card) {
		synchronized (cardImageQueue) {
			if (!cardImageQueue.contains(card))
				cardImageQueue.add(card);
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
			return saveCachedFile(file, url);
		}
	}

	/**
	 * Save url content into a local file
	 * 
	 * @param file
	 *            - local file
	 * @param url
	 *            - remote url (or any url actually)
	 * @return
	 * @throws IOException
	 */
	public static File saveCachedFile(File file, URL url) throws IOException {
		File dir = file.getParentFile();
		dir.mkdirs();
		File file2 = File.createTempFile(file.getName(), ".part", dir);
		try {
			InputStream st = null;
			try {
				st = WebUtils.openUrl(url);
			} catch (IOException e) {
				throw new IOException("Cannot connect: " + e.getMessage());
			}
			try {
				FileUtils.saveStream(st, file2);
			} catch (IOException e) {
				throw new IOException("Cannot save tmp file: " + file2 + ": " + e.getMessage());
			} finally {
				st.close();
			}
			if (file2.exists() && file2.length() > 0) {
				if (file.exists()) {
					if (!file.delete())
						throw new IOException("failed to delete " + file.toString());
				}
				if (!file2.renameTo(file)) {
					throw new IOException("failed to rename into " + file.toString());
				}
				return file;
			} else {
				throw new IOException("Cannot save file: " + file.toString());
			}
		} finally {
			file2.delete();
		}
	}

	public static URL getImageURL(IMagicCard card) throws MalformedURLException {
		String path = CardCache.createLocalImageFilePath(card);
		File file = new File(path);
		if (file.exists()) {
			return file.toURI().toURL();
		}
		URL url = createRemoteImageURL(card);
		return url;
	}

	/**
	 * Get card image or schedule a loading job if image not found. This image
	 * is not managed - to be disposed by called. To get notified when job is
	 * done loading, can wait on card object
	 * 
	 * @param card
	 * @return true if card image exists, schedule update otherwise. If loading
	 *         is disabled and there is no cached image through an exception
	 * @throws IOException
	 */
	public static boolean loadCardImageOffline(IMagicCard card, boolean forceUpdate) throws IOException, CannotDetermineSetAbbriviation {
		String path = createLocalImageFilePath(card);
		File file = new File(path);
		if (file.exists() && forceUpdate == false) {
			return true;
		}
		if (!isLoadingEnabled())
			throw new CachedImageNotFoundException("Cannot find cached image for " + card.getName());
		CardCache.queueImageLoading(card);
		return false;
	}

	public static boolean isImageCached(IMagicCard card) {
		String path = createLocalImageFilePath(card);
		File file = new File(path);
		if (file.exists()) {
			return true;
		}
		return false;
	}
}
