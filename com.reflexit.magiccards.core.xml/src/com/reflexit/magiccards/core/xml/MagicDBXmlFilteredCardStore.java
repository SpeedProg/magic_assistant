package com.reflexit.magiccards.core.xml;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.runtime.NullProgressMonitor;

import com.reflexit.magiccards.core.Activator;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicException;
import com.reflexit.magiccards.core.model.Location;
import com.reflexit.magiccards.core.model.nav.MagicDbContainter;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class MagicDBXmlFilteredCardStore extends BasicMagicDBXmlFilteredCardStore {
	private static MagicDBXmlFilteredCardStore instance;
	private ArrayList<File> files;

	private MagicDBXmlFilteredCardStore() {
		super(new VirtualMultiFileCardStore());
		instance = this;
	}

	@Override
	protected void doInitialize() throws MagicException {
		// create initial database from flat file if not there
		new XmlCardHolder().loadInitialIfNot(new NullProgressMonitor());
		// load card from xml in memory
		if (!this.table.isInitialized()) {
			synchronized (table) {
				// System.err.println("Initializing DB");
				this.files = new ArrayList<File>();
				File[] members;
				try {
					MagicDbContainter con = DataManager.getModelRoot().getMagicDBContainer();
					members = con.getFile().listFiles();
					for (File file : members) {
						if (file.getName().endsWith(".xml"))
							this.files.add(file);
					}
				} catch (MagicException e) {
					Activator.log(e);
					return;
				}
				this.table.initialize();
				this.table.setInitialized(false);
				try {
					for (File file : this.files) {
						Location setLocation = Location.createLocation(file, Location.NO_WHERE);
						this.table.addFile(file, setLocation, true);
					}
				} finally {
					this.table.setInitialized(true);
				}
			}
		}
	}

	public static IFilteredCardStore getInstance() {
		if (instance == null)
			new MagicDBXmlFilteredCardStore();
		return instance;
	}
}
