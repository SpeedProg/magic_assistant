package com.reflexit.magiccards.core.xml;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;

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

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.ICardHandler;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.model.MagicCardPhisical;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;
import com.reflexit.magiccards.core.sync.ParseGathererNewVisualSpoiler;
import com.reflexit.magiccards.core.sync.TextPrinter;

public class XmlCardHolder implements ICardHandler {
	private IFilteredCardStore activeDeck;

	public IFilteredCardStore getDatabaseHandler() {
		return MagicCardDataXmlHandler.getInstance();
	}

	public IFilteredCardStore getMyCardsHandler() {
		return LibraryDataXmlHandler.getInstance();
	}

	public IFilteredCardStore getCardCollectionHandler(String filename) {
		return new DeckXmlHandler(filename);
	}

	public ICardStore loadFromXml(String filename) {
		CollectionSingleFileCardStore store = new CollectionSingleFileCardStore(new File(filename), new Location(
		        filename), true);
		return store;
	}

	public void loadInitial() throws MagicException, CoreException, IOException {
		InputStream is = FileLocator.openStream(Activator.getDefault().getBundle(), new Path("resources/all.txt"),
		        false);
		BufferedReader st = new BufferedReader(new InputStreamReader(is, Charset.forName("utf-8")));
		loadtFromFlatIntoXml(st);
		is.close();
	}

	public static File getDbFolder() throws CoreException {
		File dir = DataManager.getModelRoot().getMagicDBContainer().getContainer().getLocation().toFile();
		return dir;
	}

	private synchronized int loadtFromFlatIntoXml(BufferedReader st) throws MagicException, IOException {
		ICardStore store = getDatabaseHandler().getCardStore();
		int init = store.size();
		ArrayList<IMagicCard> list = loadFromFlat(st);
		boolean hasAny = list.size() > 0;
		store.addAll(list);
		int rec = store.size() - init;
		return rec > 0 ? rec : (hasAny ? 0 : -1);
	}

	private ArrayList<IMagicCard> loadFromFlat(BufferedReader st) throws IOException {
		String line;
		st.readLine(); // header ignore for now
		ArrayList<IMagicCard> list = new ArrayList<IMagicCard>();
		HashSet<Integer> hash = new HashSet<Integer>();
		while ((line = st.readLine()) != null) {
			String[] fields = line.split("\\Q" + TextPrinter.SEPARATOR);
			for (int i = 0; i < fields.length; i++) {
				fields[i] = fields[i].trim();
			}
			MagicCard card = new MagicCard();
			card.setId(fields[0]);
			card.setName(fields[1]);
			card.setCost(fields[2]);
			card.setType(fields[3]);
			card.setPower(fields[4]);
			card.setToughness(fields[5]);
			card.setOracleText(fields[6]);
			card.setSet(fields[7]);
			card.setRarity(fields[8]);
			if (fields.length > 9)
				card.setColorType(fields[9]);
			if (fields.length > 10)
				card.setCmc(fields[10]);
			card.setExtraFields();
			int id = card.getCardId();
			if (id == 0) {
				System.err.print("Skipped invalid: ");
				TextPrinter.print(card, System.err);
				continue;
			}
			if (hash.contains(id))
				continue;
			hash.add(id);
			list.add(card);
		}
		return list;
	}

	public void loadInitialIfNot(IProgressMonitor pm) throws MagicException {
		pm.beginTask("Init", 100);
		try {
			IContainer db = DataManager.getModelRoot().getMagicDBContainer().getContainer();
			db.refreshLocal(1, new SubProgressMonitor(pm, 50));
			IResource[] members = db.members();
			if (members.length > 0)
				return;
			else
				loadInitial();
		} catch (Exception e) {
			throw new MagicException(e);
		} finally {
			pm.done();
		}
	}

	public String download(String set, Properties options, IProgressMonitor pm) throws FileNotFoundException,
	        MalformedURLException, IOException {
		IPath path = Activator.getStateLocationAlways().append("downloaded.txt");
		String file = path.toPortableString();
		ParseGathererNewVisualSpoiler.downloadUpdates(set, file, options, pm);
		return file;
	}

	public int downloadUpdates(String set, Properties options, IProgressMonitor pm) throws MagicException,
	        InterruptedException {
		int rec;
		try {
			pm.beginTask("Downloading", 100);
			pm.subTask("Initializing");
			loadInitialIfNot(new SubProgressMonitor(pm, 30));
			if (pm.isCanceled())
				throw new InterruptedException();
			pm.subTask("Downloading cards...");
			String file = download(set, options, new SubProgressMonitor(pm, 30));
			if (pm.isCanceled())
				throw new InterruptedException();
			pm.subTask("Updating database...");
			BufferedReader st = new BufferedReader(new FileReader(file));
			rec = loadtFromFlatIntoXml(st);
			st.close();
			pm.worked(30);
			pm.subTask("Updating editions...");
			Editions.getInstance().save();
			pm.worked(10);
			return rec;
		} catch (MalformedURLException e) {
			throw new MagicException(e);
		} catch (IOException e) {
			throw new MagicException(e);
		}
	}

	public boolean copyCards(Collection cards, Location to) {
		ICardStore<IMagicCard> store = LibraryDataXmlHandler.getInstance().getStore(to);
		if (store == null)
			return false;
		ArrayList<IMagicCard> list = new ArrayList<IMagicCard>(cards.size());
		for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			MagicCardPhisical phi = new MagicCardPhisical(card, to);
			if (card instanceof MagicCard) // moving from db
				phi.setOwn(!store.isVirtual());
			list.add(phi);
		}
		return store.addAll(cards);
	}

	public boolean moveCards(Collection cards, Location from, Location to) {
		ICardStore<IMagicCard> sto = LibraryDataXmlHandler.getInstance().getStore(to);
		if (sto == null)
			return false;
		ArrayList<IMagicCard> list = new ArrayList<IMagicCard>(cards.size());
		for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
			IMagicCard card = (IMagicCard) iterator.next();
			MagicCardPhisical phi = new MagicCardPhisical(card, to);
			if (card instanceof MagicCard) // moving from db
				phi.setOwn(!sto.isVirtual());
			list.add(phi);
		}
		boolean res = sto.addAll(list);
		if (res) {
			boolean allthesame = true;
			if (from == null) {
				for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
					IMagicCard card = (IMagicCard) iterator.next();
					if (!(card instanceof MagicCardPhisical))
						break;
					Location from2 = ((MagicCardPhisical) card).getLocation();
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
				ICardStore<IMagicCard> sfrom2 = LibraryDataXmlHandler.getInstance().getStore(from);
				if (sfrom2 != null)
					sfrom2.removeAll(cards);
			} else {
				for (Iterator iterator = cards.iterator(); iterator.hasNext();) {
					IMagicCard card = (IMagicCard) iterator.next();
					if (!(card instanceof MagicCardPhisical))
						continue;
					Location from2 = ((MagicCardPhisical) card).getLocation();
					ICardStore<IMagicCard> sfrom2 = LibraryDataXmlHandler.getInstance().getStore(from2);
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
