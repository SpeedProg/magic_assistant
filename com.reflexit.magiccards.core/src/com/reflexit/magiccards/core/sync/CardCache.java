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

import com.reflexit.magiccards.core.CachedImageNotFoundException;
import com.reflexit.magiccards.core.CannotDetermineSetAbbriviation;
import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.NotNull;
import com.reflexit.magiccards.core.model.Edition;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;

/**
 * @author Alena
 *
 */
public class CardCache {
	public static URL createSetImageURL(IMagicCard card, boolean upload) throws IOException {
		String edition = card.getSet();
		String rarity = card.getRarity();
		return createSetImageURL(edition, rarity, upload);
	}

	private static URL createSetImageURL(String editionName, String rarity, boolean upload)
			throws MalformedURLException, IOException {
		Edition edition = Editions.getInstance().getEditionByName(editionName);
		if (edition == null)
			return null;
		return edition.getImageFiles().getLocalURL(rarity, upload);
	}

	public static URL createRemoteImageURL(IMagicCard card) throws MalformedURLException {
		String strUrl = (String) card.get(MagicCardField.IMAGE_URL);
		if (strUrl == null)
			return null;
		return new URL(strUrl);
	}

	@NotNull
	public static String createLocalImageFilePath(IMagicCard card) {
		return createLocalImageFilePath(card.getCardId(), card.getEdition().getMainAbbreviation());
	}

	@NotNull
	public static String createLocalImageFilePath(int cardId, String abbr) {
		String editionAbbr = abbr;
		if (abbr == null)
			editionAbbr = "unknown";
		else if (abbr.equals("CON")) // special hack for windows, which cannot
										// create CON directory
			editionAbbr = "CONFL";
		String part;
		if (cardId == 0) {
			part = "Cards/0.jpg";
		} else {
			part = "Cards/" + editionAbbr + "/" + "EN" + "/Card" + cardId + ".jpg"; // XXX
																					// remove
																					// EN
		}
		return new File(FileUtils.getStateLocationFile(), part).getPath();
	}

	private static boolean isLoadingEnabled() {
		return !WebUtils.isWorkOffline();
	}

	private static ArrayList<IMagicCard> cardImageQueue = new ArrayList<IMagicCard>();
	private static Thread cardImageLoadingJob = null;

	static synchronized void initCardImageLoading() {
		if (cardImageLoadingJob != null)
			return;
		cardImageLoadingJob = new Thread("Loading card images") {
			@Override
			public void run() {
				while (true) {
					IMagicCard card = null;
					synchronized (cardImageQueue) {
						if (cardImageQueue.size() > 0) {
							card = cardImageQueue.get(0);
							cardImageQueue.remove(0);
							cardImageQueue.notifyAll();
						} else {
							try {
								cardImageQueue.wait(10000);
							} catch (InterruptedException e) {
								break;
							}
							if (cardImageQueue.size() > 0)
								continue;
							break;
						}
					}
					if (card == null)
						continue;
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
				synchronized (CardCache.class) {
					cardImageLoadingJob = null;
				}
			}
		};
		cardImageLoadingJob.start();
	}

	private static void queueImageLoading(IMagicCard card) {
		initCardImageLoading();
		synchronized (cardImageQueue) {
			if (!cardImageQueue.contains(card)) {
				cardImageQueue.add(card);
				cardImageQueue.notifyAll();
			} else {
				card.notifyAll();
			}
		}
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
			if (url == null)
				throw new CachedImageNotFoundException("Cannot find image for " + card.getName() + " not url");
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
	public static boolean loadCardImageOffline(IMagicCard card, boolean forceUpdate)
			throws IOException, CannotDetermineSetAbbriviation {
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
