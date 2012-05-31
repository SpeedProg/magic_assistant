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
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.ICardHandler;
import com.reflexit.magiccards.core.model.ICardModifiable;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardFieldPhysical;
import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.monitor.ICoreProgressMonitor;
import com.reflexit.magiccards.core.monitor.SubCoreProgressMonitor;
import com.reflexit.magiccards.core.sync.ParseGathererNewVisualSpoiler;
import com.reflexit.magiccards.core.sync.ParseGathererSets;
import com.reflexit.magiccards.core.sync.TextPrinter;
import com.reflexit.magiccards.core.sync.UpdateCardsFromWeb;

public class XmlCardHolder implements ICardHandler {
	private IFilteredCardStore activeDeck;

	public IFilteredCardStore getMagicDBFilteredStore() {
		return MagicDBXmlFilteredCardStore.getInstance();
	}

	public ICardStore getMagicDBStore() {
		return getMagicDBFilteredStore().getCardStore();
	}

	public IFilteredCardStore getMagicDBFilteredStoreWorkingCopy() {
		return new BasicMagicDBXmlFilteredCardStore((VirtualMultiFileCardStore) getMagicDBFilteredStore().getCardStore());
	}

	public IFilteredCardStore getLibraryFilteredStore() {
		return LibraryXmlFilteredCardStore.getInstance();
	}

	public IFilteredCardStore getLibraryFilteredStoreWorkingCopy() {
		return new BasicLibraryXmlFilteredCardStore((CollectionMultiFileCardStore) getLibraryCardStore());
	}

	public ICardStore getLibraryCardStore() {
		return getLibraryFilteredStore().getCardStore();
	}

	public IFilteredCardStore getCardCollectionFilteredStore(String filename) {
		return new DeckXmlFilteredCardStore(filename);
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
				long time = System.currentTimeMillis();
				loadFromFlatResource(abbr + ".txt");
				System.err.println("Loading " + abbr + " took " + (System.currentTimeMillis() - time) / 1000 + " s");
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
			loadtFromFlatIntoXml(st, list, isSingleSet(set));
			is.close();
		}
	}

	private boolean isSingleSet(String set) {
		if (set.equalsIgnoreCase("Standard"))
			return false;
		if (set.equalsIgnoreCase("All"))
			return false;
		return true;
	}

	public static File getDbFolder() {
		File dir = DataManager.getModelRoot().getMagicDBContainer().getFile();
		return dir;
	}

	private synchronized int loadtFromFlatIntoXml(BufferedReader st, ArrayList<IMagicCard> list, boolean markCn) throws MagicException,
			IOException {
		ICardStore store = getMagicDBFilteredStore().getCardStore();
		int init = store.size();
		loadFromFlat(st, list, markCn);
		boolean hasAny = list.size() > 0;
		store.addAll(list);
		// ArrayList<IMagicCard> more = fixCards(list);
		// if (more.size() > 0)
		// store.addAll(more);
		int rec = store.size() - init;
		return rec > 0 ? rec : (hasAny ? 0 : -1);
	}

	// private ArrayList<IMagicCard> fixCards(ArrayList<IMagicCard> list) {
	// ArrayList<IMagicCard> more = new ArrayList<IMagicCard>();
	// try {
	// for (int i = 0; i < list.size(); i++) {
	// MagicCard card = (MagicCard) list.get(i);
	// card.setExtraFields();
	// if (card.getPart() != null) {
	// MagicCard brother = null;
	// if (i + 1 < list.size()) {
	// MagicCard next = (MagicCard) list.get(i + 1);
	// if (next.getName().equals(card.getName())) {
	// brother = next;
	// i++;
	// }
	// }
	// if (brother == null) {
	// // no brother - they have same mid
	// brother = card.cloneCard();
	// flipParts(brother);
	// more.add(brother);
	// } else {
	// brother.setExtraFields();
	// if (card.getCardId() < brother.getCardId()) {
	// flipParts(brother, card.getCardId());
	// } else {
	// flipParts(card, brother.getCardId());
	// }
	// }
	// }
	// }
	// } catch (Exception e) {
	// MagicLogger.log(e);
	// }
	// return more;
	// }
	protected void flipParts(MagicCard card) {
		String opart = card.getPart();
		String part = (String) card.getObjectByField(MagicCardField.OTHER_PART);
		((ICardModifiable) card).setObjectByField(MagicCardField.PART, part);
		((ICardModifiable) card).setObjectByField(MagicCardField.OTHER_PART, opart);
		((ICardModifiable) card).setObjectByField(MagicCardField.NAME, card.getName().replaceAll("\\Q(" + opart + ")", "(" + part + ")"));
	}

	private ArrayList<IMagicCard> loadFromFlat(BufferedReader st, ArrayList<IMagicCard> list, boolean markCn) throws IOException {
		String line = st.readLine(); // header ignore for now
		ICardField[] xfields = MagicCardFieldPhysical.toFields(line, "\\Q" + TextPrinter.SEPARATOR);
		int cnum = 0;
		while ((line = st.readLine()) != null) {
			cnum++;
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
				if (markCn && card.getCollNumber() == null || card.getCollNumber().length() == 0) {
					card.setCollNumber(cnum);
				}
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
			if (dir.listFiles().length > 0)
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
		ParseGathererNewVisualSpoiler.downloadUpdates(set, file, options, pm);
		return file;
	}

	public int downloadUpdates(String set, Properties options, ICoreProgressMonitor pm) throws MagicException, InterruptedException {
		int rec;
		try {
			String lang = (String) options.get(ParseGathererNewVisualSpoiler.UPDATE_LANGUAGE);
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
			pm.subTask("Downloading cards...");
			String file = download(set, options, new SubCoreProgressMonitor(pm, 50));
			if (pm.isCanceled())
				throw new InterruptedException();
			pm.subTask("Updating database...");
			BufferedReader st = new BufferedReader(new FileReader(file));
			ArrayList<IMagicCard> list = new ArrayList<IMagicCard>();
			rec = loadtFromFlatIntoXml(st, list, isSingleSet(set));
			st.close();
			pm.worked(30);
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

	public boolean copyCards(Collection cards, Location to) {
		ICardStore<IMagicCard> store = LibraryXmlFilteredCardStore.getInstance().getStore(to);
		if (store == null)
			return false;
		boolean virtual = store.isVirtual();
		ArrayList<IMagicCard> list = new ArrayList<IMagicCard>(cards.size());
		for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			MagicCardPhysical phi = new MagicCardPhysical(card, to);
			if (card instanceof MagicCard) // moving from db
				phi.setOwn(!virtual);
			else if (virtual) {
				phi.setOwn(false); // copied cards will have collection
									// ownership for virtual
			}
			list.add(phi);
		}
		return store.addAll(list);
	}

	public boolean moveCards(Collection cards, Location from, Location to) {
		ICardStore<IMagicCard> sto = LibraryXmlFilteredCardStore.getInstance().getStore(to);
		if (sto == null)
			return false;
		boolean virtual = sto.isVirtual();
		ArrayList<IMagicCard> list = new ArrayList<IMagicCard>(cards.size());
		for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			MagicCardPhysical phi = new MagicCardPhysical(card, to);
			if (card instanceof MagicCard) {
				phi.setOwn(!virtual);
			} else if (card instanceof MagicCardPhysical) {
				if (((MagicCardPhysical) card).isOwn() && virtual)
					throw new MagicException("Cannot move own cards to virtual collection. Use copy instead.");
			}
			list.add(phi);
		}
		boolean res = sto.addAll(list);
		if (res) {
			boolean allthesame = true;
			if (from == null) {
				for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
					IMagicCard card = (IMagicCard) iterator.next();
					if (!(card instanceof MagicCardPhysical))
						break;
					Location from2 = ((MagicCardPhysical) card).getLocation();
					if (from2 != null) {
						if (from == null)
							from = from2;
						else if (!from.equals(from2)) {
							allthesame = false;
							break;
						}
					}
				}
			}
			if (from != null && allthesame) {
				ICardStore<IMagicCard> sfrom2 = LibraryXmlFilteredCardStore.getInstance().getStore(from);
				if (sfrom2 != null)
					sfrom2.removeAll(cards);
			} else {
				for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
					IMagicCard card = (IMagicCard) iterator.next();
					if (!(card instanceof MagicCardPhysical))
						continue;
					Location from2 = ((MagicCardPhysical) card).getLocation();
					ICardStore<IMagicCard> sfrom2 = LibraryXmlFilteredCardStore.getInstance().getStore(from2);
					sfrom2.remove(card);
				}
			}
		}
		return res;
	}

	public IFilteredCardStore getActiveDeckHandler() {
		return this.activeDeck;
	}

	public void setActiveDeckHandler(IFilteredCardStore store) {
		this.activeDeck = store;
	}
}
