package com.reflexit.magiccards.core.sql.handlers;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.ICardHandler;
import com.reflexit.magiccards.core.model.IFilteredCardStore;
import com.reflexit.magiccards.core.sql.LoadMagicDb;
import com.reflexit.magiccards.core.sync.ParseGathererSpoiler;

public class CardHolder implements ICardHandler {
	public IFilteredCardStore getMagicCardHandler() {
		return MagicCardDataHandler.getInstance();
	}

	public IFilteredCardStore getMagicLibraryHandler() {
		return LibraryDataHandler.getInstance();
	}

	public void loadInitial() throws MagicException {
		try {
			LoadMagicDb.loadInitial();
		} catch (IOException e) {
			throw new MagicException(e);
		} catch (SQLException e) {
			throw new MagicException(e);
		}
	}

	public void loadInitialIfNot(IProgressMonitor pm) throws MagicException {
		pm.beginTask("Init", 100);
		boolean isLoaded = Activator.getDefault().getBooleanPreference(Activator.DB_LOADED);
		if (isLoaded == false) {
			loadInitial();
			Activator.getDefault().setPreference(Activator.DB_LOADED, true);
		}
		pm.done();
	}

	public String upload(String url, IProgressMonitor pm) throws FileNotFoundException, MalformedURLException,
	        IOException {
		IPath path = Activator.getStateLocationAlways().append("uploaded.txt");
		String file = path.toPortableString();
		ParseGathererSpoiler.parseFileOrUrl(url, file);
		return file;
	}

	public int downloadFromUrl(String url, IProgressMonitor pm) throws MagicException, InterruptedException {
		int rec;
		try {
			pm.beginTask("Updating", 100);
			pm.subTask("Initializing");
			loadInitialIfNot(new SubProgressMonitor(pm, 30));
			if (pm.isCanceled())
				throw new InterruptedException();
			pm.subTask("Uploading");
			String file = upload(url, pm);
			if (pm.isCanceled())
				throw new InterruptedException();
			pm.worked(30);
			pm.subTask("Updating database");
			BufferedReader st = new BufferedReader(new FileReader(file));
			rec = LoadMagicDb.updateTable(st);
			st.close();
			pm.worked(30);
			pm.subTask("Updating editions");
			Editions.getInstance().save();
			pm.worked(10);
			return rec;
		} catch (MalformedURLException e) {
			throw new MagicException(e);
		} catch (IOException e) {
			throw new MagicException(e);
		} catch (SQLException e) {
			throw new MagicException(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.reflexit.magiccards.core.model.ICardHandler#getDeckHandler(java.lang.String)
	 */
	public IFilteredCardStore getDeckHandler(String id) {
		throw new UnsupportedOperationException();
	}
}
