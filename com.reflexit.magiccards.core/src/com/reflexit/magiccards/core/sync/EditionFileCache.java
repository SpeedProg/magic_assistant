package com.reflexit.magiccards.core.sync;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.OfflineException;
import com.reflexit.magiccards.core.model.Edition;
import com.reflexit.magiccards.core.model.Rarity;

public class EditionFileCache {
	private Edition edition;
	private HashMap<String, CachedFile> map = new HashMap<>(3);

	public EditionFileCache(Edition ed) {
		this.edition = ed;
	}

	public CachedFile getImageCachedFile(String rarity, boolean forceRemote) throws IOException {
		if ("Land".equals(rarity))
			rarity = "Common";
		CachedFile cachedFile = map.get(rarity);
		if (cachedFile != null) {
			if (forceRemote == false || cachedFile.getRemoteURL() != null)
				return cachedFile;
		}
		String path = createLocalSetImageFilePath(rarity);
		if (forceRemote == false) {
			cachedFile = new CachedFile(null, new File(path));
			if (cachedFile.isImage()) {
				map.put(rarity, cachedFile);
				return cachedFile;
			}
		}
		String tryRarity = rarity;
		while (tryRarity != null) {
			String[] abbreviations = edition.getAbbreviations();
			for (int i = 0; i < abbreviations.length; i++) {
				String editionAbbr = abbreviations[i];
				URL url = createSetImageRemoteURL(editionAbbr, tryRarity);
				cachedFile = new CachedFile(url, new File(path));
				cachedFile.recache();
				if (cachedFile.isImage()) {
					map.put(rarity, cachedFile);
					return cachedFile;
				}
			}
			tryRarity = Rarity.getMoreRare(tryRarity);
		}
		return null;
	}

	public URL getLocalURL(String rarity, boolean upload) throws IOException {
		if (upload == false)
			return createSetImageLocalURL(rarity);
		CachedFile onefile = getImageCachedFile(rarity, false);
		if (onefile != null)
			return onefile.getLocalURL(false);
		return createSetImageLocalURL(rarity);
	}

	public URL getRemoteURL(String rarity, boolean force) throws IOException {
		try {
			CachedFile onefile = getImageCachedFile(rarity, false);
			URL url = null;
			if (onefile != null)
				url = onefile.getRemoteURL();
			if (url == null) {
				onefile = getImageCachedFile(rarity, true);
				if (onefile != null)
					url = onefile.getRemoteURL();
			}
			if (url != null)
				return url;
		} catch (OfflineException e) {
			// ignore return pre-defined unchecked url
		}
		return createSetImageRemoteURL(edition.getMainAbbreviation(), rarity);
	}

	public URL createSetImageLocalURL(String rarity) {
		String path = createLocalSetImageFilePath(rarity);
		URL localUrl = null;
		try {
			localUrl = new File(path).toURI().toURL();
		} catch (MalformedURLException e) {
			// should not happen
		}
		return localUrl;
	}

	public static URL createSetImageRemoteURL(String editionAbbr, String rarity) {
		return GatherHelper.createSetImageURL(editionAbbr, rarity);
	}

	private String createLocalSetImageFilePath(String rarity) {
		if ("Land".equals(rarity))
			rarity = "Common";
		File loc = FileUtils.getStateLocationFile();
		String name = edition.getBaseFileName() + "-" + rarity;
		String part = "Sets/" + name + ".png";
		String file = new File(loc, part).getPath();
		return file;
	}
}
