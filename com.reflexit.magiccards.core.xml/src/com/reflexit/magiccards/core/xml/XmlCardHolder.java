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
import java.util.ArrayList;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.ICardHandler;
import com.reflexit.magiccards.core.model.IFilteredCardStore;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.MagicCard;
import com.reflexit.magiccards.core.sync.ParseGathererSpoiler;
import com.reflexit.magiccards.core.sync.TextPrinter;

public class XmlCardHolder implements ICardHandler {
	public IFilteredCardStore getMagicCardHandler() {
		return MagicCardDataXmlHandler.getInstance();
	}

	public IFilteredCardStore getMagicLibraryHandler() {
		return LibraryDataXmlHandler.getInstance();
	}

	public IFilteredCardStore getDeckHandler(String filename) {
		return new DeckXmlHandler(filename);
	}

	public void loadInitial() throws MagicException, CoreException, IOException {
		InputStream is = FileLocator.openStream(Activator.getDefault().getBundle(), new Path("resources/all.txt"),
		        false);
		BufferedReader st = new BufferedReader(new InputStreamReader(is));
		loadtFromFlatIntoXml(st);
		is.close();
	}

	public static File getDbFolder() throws CoreException {
		File dir = DataManager.getModelRoot().getMagicDBContainer().getContainer().getLocation().toFile();
		return dir;
	}

	private int loadtFromFlatIntoXml(BufferedReader st) throws MagicException, IOException {
		int init = getMagicCardHandler().getCardStore().getTotal();
		ArrayList<IMagicCard> list = loadFromFlat(st);
		boolean hasAny = list.size() > 0;
		getMagicCardHandler().getCardStore().addAll(list);
		int rec = getMagicCardHandler().getCardStore().getTotal() - init;
		return rec > 0 ? rec : (hasAny ? 0 : -1);
	}

	private ArrayList<IMagicCard> loadFromFlat(BufferedReader st) throws IOException {
		String line;
		st.readLine(); // header ignore for now
		ArrayList<IMagicCard> list = new ArrayList<IMagicCard>();
		while ((line = st.readLine()) != null) {
			String[] fields = line.split("\\Q" + TextPrinter.SEPARATOR);
			for (int i = 0; i < fields.length; i++) {
				fields[i] = fields[i].trim();
			}
			MagicCard card = new MagicCard();
			card.setValues(fields);
			card.setExtraFields();
			int id = card.getCardId();
			if (id == 0) {
				System.err.print("Skipped invalid: ");
				TextPrinter.print(card, System.err);
				continue;
			}
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

	public String download(String url, IProgressMonitor pm) throws FileNotFoundException, MalformedURLException,
	        IOException {
		IPath path = Activator.getStateLocationAlways().append("downloaded.txt");
		String file = path.toPortableString();
		ParseGathererSpoiler.parseFileOrUrl(url, file);
		return file;
	}

	public int downloadFromUrl(String url, IProgressMonitor pm) throws MagicException, InterruptedException {
		int rec;
		try {
			pm.beginTask("Downloading", 100);
			pm.subTask("Initializing");
			loadInitialIfNot(new SubProgressMonitor(pm, 30));
			if (pm.isCanceled())
				throw new InterruptedException();
			pm.subTask("Downloading cards...");
			String file = download(url, pm);
			if (pm.isCanceled())
				throw new InterruptedException();
			pm.worked(30);
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
}
