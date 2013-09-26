package com.reflexit.magiccards.core.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Editions.Edition;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.ICardHandler;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.monitor.SubCoreProgressMonitor;
import com.reflexit.magiccards.core.sync.ParseGathererSets;
import com.reflexit.magiccards.core.sync.TextPrinter;
import com.reflexit.magiccards.core.sync.UpdateCardsFromWeb;

public class XmlCardHolder implements ICardHandler {
	private IFilteredCardStore activeDeck;

	public IFilteredCardStore getMagicDBFilteredStore() {
		return MagicDBFilteredCardFileStore.getInstance();
	}

	public IDbCardStore getMagicDBStore() {
		return (IDbCardStore) getMagicDBFilteredStore().getCardStore();
	}

	public IFilteredCardStore getMagicDBFilteredStoreWorkingCopy() {
		return new BasicMagicDBFilteredCardFileStore((DbMultiFileCardStore) getMagicDBStore());
	}

	public IFilteredCardStore getLibraryFilteredStore() {
		return LibraryFilteredCardFileStore.getInstance();
	}

	@Override
	public ICardStore getCardStore(Location to) {
		return LibraryFilteredCardFileStore.getInstance().getStore(to);
	}

	public IFilteredCardStore getLibraryFilteredStoreWorkingCopy() {
		return new BasicLibraryFilteredCardFileStore((CollectionMultiFileCardStore) getLibraryCardStore());
	}

	public ICardStore getLibraryCardStore() {
		return getLibraryFilteredStore().getCardStore();
	}

	public IFilteredCardStore getCardCollectionFilteredStore(String filename) {
		return new DeckFilteredCardFileStore(filename);
	}

	public ICardStore loadFromXml(String filename) {
		CollectionSingleFileCardStore store = new CollectionSingleFileCardStore(new File(filename), new Location(filename), true);
		return store;
	}

	public void loadInitial() throws MagicException {
		// loadFromFlatResource("all.txt");
		Collection<String> editions = Editions.getInstance().getNames();
		for (String set : editions) {
			String abbr = (Editions.getInstance().getEditionByName(set).getBaseFileName());
			try {
				// long time = System.currentTimeMillis();
				loadFromFlatResource(abbr + ".txt");
				// long nowtime = System.currentTimeMillis() - time;
				// System.err.println("Loading " + abbr + " took " + nowtime / 1000 + " s " +
				// nowtime % 1000 + " ms");
			} catch (IOException e) {
				// ignore
			}
		}
	}

	protected void loadFromFlatResource(String set) throws IOException {
		InputStream is = FileUtils.loadDbResource(set);
		if (is != null) {
			BufferedReader st = new BufferedReader(new InputStreamReader(is, Charset.forName("utf-8")));
			ArrayList<IMagicCard> list = new ArrayList<IMagicCard>();
			loadtFromFlatIntoXml(st, list);
			is.close();
		}
	}

	public static File getDbFolder() {
		File dir = DataManager.getModelRoot().getMagicDBContainer().getFile();
		return dir;
	}

	private synchronized int loadtFromFlatIntoXml(BufferedReader st, ArrayList<IMagicCard> list) throws MagicException, IOException {
		ICardStore store = getMagicDBStore();
		int init = store.size();
		loadFromFlat(st, list);
		boolean hasAny = list.size() > 0;
		store.addAll(list);
		// ArrayList<IMagicCard> more = fixCards(list);
		// if (more.size() > 0)
		// store.addAll(more);
		int rec = store.size() - init;
		return rec > 0 ? rec : (hasAny ? 0 : -1);
	}

	private ArrayList<IMagicCard> loadFromFlat(BufferedReader st, ArrayList<IMagicCard> list) throws IOException {
		String line = st.readLine(); // header ignore for now
		ICardField[] xfields = MagicCardFieldPhysical.toFields(line, "\\Q" + TextPrinter.SEPARATOR);
		while ((line = st.readLine()) != null) {
			if (line.length() == 0)
				continue;
			try {
				String[] fields = linesplit(line, TextPrinter.SEPARATOR);
				for (int i = 0; i < fields.length; i++) {
					fields[i] = fields[i].trim();
				}
				MagicCard card = new MagicCard();
				int i = 0;
				for (ICardField field : xfields) {
					if (i < fields.length) {
						card.setObjectByField(field, fields[i]);
					}
					i++;
				}
				// if (markCn && (card.getCollNumber() == null || card.getCollNumber().length() ==
				// 0)) {
				// card.setCollNumber(cnum);
				// }
				int id = card.getCardId();
				if (id == 0) {
					System.err.print("Skipped invalid: ");
					TextPrinter.print(card, System.err);
					continue;
				}
				list.add(card);
			} catch (Exception e) {
				MagicLogger.log(e);
			}
		}
		return list;
	}

	/**
	 * Optimized split function
	 * 
	 * @param line
	 * @param ssep
	 * @return
	 */
	private String[] linesplit(String line, String ssep) {
		char sep = ssep.charAt(0);
		int k = 0;
		int len = line.length();
		for (int i = 0; i < len; i++) {
			if (line.charAt(i) == sep) {
				k++;
			}
		}
		String res[] = new String[k + 1];
		int ik = 0;
		int n = line.indexOf(sep);
		if (n < 0)
			n = len;
		res[ik++] = line.substring(0, n);
		for (int i = n; i < len;) {
			n = line.indexOf(sep, i + 1);
			if (n < 0)
				n = len;
			res[ik++] = line.substring(i + 1, n);
			i = n;
		}
		return res;
	}

	public synchronized void loadInitialIfNot(ICoreProgressMonitor pm) throws MagicException {
		pm.beginTask("Init", 100);
		try {
			File dir = DataManager.getModelRoot().getMagicDBContainer().getFile();
			File[] listFiles = dir.listFiles();
			if (listFiles != null && listFiles.length > 0)
				return;
			else
				loadInitial();
		} catch (Exception e) {
			throw new MagicException(e);
		} finally {
			pm.done();
		}
	}

	public String download(String set, Properties options, ICoreProgressMonitor pm) throws FileNotFoundException, MalformedURLException,
			IOException {
		String file = new File(FileUtils.getStateLocationFile(), "downloaded.txt").getPath();
		UpdateCardsFromWeb.downloadUpdates(set, file, options, pm);
		return file;
	}

	public int downloadUpdates(String set, Properties options, ICoreProgressMonitor pm) throws MagicException, InterruptedException {
		int rec;
		try {
			String lang = (String) options.get(UpdateCardsFromWeb.UPDATE_LANGUAGE);
			if (lang != null && lang.length() == 0) {
				lang = null;
			}
			pm.beginTask("Downloading", 110 + (lang == null ? 0 : 100));
			pm.subTask("Initializing");
			loadInitialIfNot(new SubCoreProgressMonitor(pm, 10));
			if (pm.isCanceled())
				throw new InterruptedException();
			pm.subTask("Updating set list...");
			try {
				new ParseGathererSets().load(new SubCoreProgressMonitor(pm, 10));
				Editions.getInstance().save();
			} catch (Exception e) {
				MagicLogger.log(e); // move on if exception via set loading
			}
			ArrayList<IMagicCard> list = new ArrayList<IMagicCard>();
			pm.subTask("Downloading cards...");
			rec = downloadAndStore(set, options, list, pm);
			pm.subTask("Updating editions...");
			Editions.getInstance().save();
			pm.worked(10);
			if (lang != null && lang.length() > 0) {
				pm.subTask("Updating languages...");
				Set<ICardField> fieldMaps = new HashSet<ICardField>();
				fieldMaps.add(MagicCardField.LANG);
				new UpdateCardsFromWeb().updateStore(list.iterator(), list.size(), fieldMaps, lang, getMagicDBStore(),
						new SubCoreProgressMonitor(pm, 100));
			}
			return rec;
		} catch (MalformedURLException e) {
			throw new MagicException(e);
		} catch (IOException e) {
			throw new MagicException(e);
		}
	}

	public int downloadAndStore(String set, Properties options, ArrayList<IMagicCard> list, ICoreProgressMonitor pm)
			throws FileNotFoundException, MalformedURLException, IOException, InterruptedException {
		int rec = 0;
		if (set.equalsIgnoreCase("All")) {
			Collection<Edition> editions = Editions.getInstance().getEditions();
			pm.beginTask("Downloading all cards", editions.size() * 1000);
			try {
				int i = 1;
				int n = editions.size();
				for (Iterator iterator = editions.iterator(); iterator.hasNext(); i++) {
					Edition edition = (Edition) iterator.next();
					try {
						pm.setTaskName("Downloading " + edition.getName() + " (" + i + " of " + n + ")");
						rec += downloadAndStoreSet(edition.getName(), options, list, new SubCoreProgressMonitor(pm, 1000));
					} catch (InterruptedException e) {
						throw e;
					} catch (Exception e) {
						MagicLogger.log(e);
					}
					if (pm.isCanceled())
						throw new InterruptedException();
				}
			} finally {
				pm.done();
			}
			return rec;
		} else {
			return downloadAndStoreSet(set, options, list, new SubCoreProgressMonitor(pm, 100));
		}
	}

	public int downloadAndStoreSet(String set, Properties options, ArrayList<IMagicCard> list, ICoreProgressMonitor pm)
			throws FileNotFoundException, MalformedURLException, IOException, InterruptedException {
		pm.beginTask("Downloading set", 100);
		try {
			int rec;
			String file = download(set, options, new SubCoreProgressMonitor(pm, 70));
			if (pm.isCanceled())
				throw new InterruptedException();
			pm.subTask("Updating database for " + set);
			BufferedReader st = new BufferedReader(new FileReader(file));
			rec = loadtFromFlatIntoXml(st, list);
			st.close();
			pm.worked(30);
			return rec;
		} finally {
			pm.done();
		}
	}

	public IFilteredCardStore getActiveDeckHandler() {
		return this.activeDeck;
	}

	public void setActiveDeckHandler(IFilteredCardStore store) {
		this.activeDeck = store;
	}
}
